package com.smartfitness.search.internal.logic;

import com.smartfitness.event.BranchPreferenceCreatedEvent;
import com.smartfitness.messaging.IMessagePublisherService;
import com.smartfitness.search.internal.index.BranchPreferenceIndex;
import com.smartfitness.search.internal.pipeline.LlmKeywordExtractionStage;
import com.smartfitness.search.internal.pipeline.MatchIndexUpdateStage;
import com.smartfitness.search.internal.query.QueryKeywordTokenizer;
import com.smartfitness.search.model.BranchRecommendation;
import com.smartfitness.search.model.ContentType;
import com.smartfitness.search.model.SearchQuery;
import com.smartfitness.search.ports.ILLMAnalysisService;
import com.smartfitness.search.ports.ISearchRepository;
import com.smartfitness.search.ports.ISearchServiceApi;
import java.util.List;
import java.util.Objects;

/**
 * BranchContentService: Orchestrates search with DD-06 architecture.
 * 
 * DD-06 Implementation (Approach C - 전문 검색 엔진):
 * 
 * Cold Path (Async Pre-indexing):
 * - registerContent() → LLM 분석 → PreferenceMatchConsumer → 인덱싱
 * - Uses PreferenceMatchScheduler for batch processing during off-peak hours
 * 
 * Hot Path (Fast Search):
 * - searchBranches() → Simple tokenization → Direct search engine query
 * - NO external LLM calls (guarantees QAS-03: 3 seconds)
 * - Uses TF-IDF ranking for fast, local results
 */
public class BranchContentService implements ISearchServiceApi {
    private final ISearchRepository repository;
    private final LlmKeywordExtractionStage llmStage;
    private final MatchIndexUpdateStage indexStage;
    private final QueryKeywordTokenizer queryTokenizer;
    private final IMessagePublisherService messagePublisher;
    private final BranchPreferenceIndex branchPreferenceIndex;

    public BranchContentService(ISearchRepository repository,
                                ILLMAnalysisService llmClient,
                                IMessagePublisherService messagePublisher) {
        this(repository, llmClient, messagePublisher, new BranchPreferenceIndex());
    }

    public BranchContentService(ISearchRepository repository,
                                ILLMAnalysisService llmClient,
                                IMessagePublisherService messagePublisher,
                                BranchPreferenceIndex branchPreferenceIndex) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.llmStage = new LlmKeywordExtractionStage(Objects.requireNonNull(llmClient, "llmClient"));
        this.indexStage = new MatchIndexUpdateStage(this.repository, branchPreferenceIndex);
        this.queryTokenizer = new QueryKeywordTokenizer();
        this.messagePublisher = Objects.requireNonNull(messagePublisher, "messagePublisher");
        this.branchPreferenceIndex = branchPreferenceIndex;
    }

    /**
     * Hot Path: Fast search using pre-indexed keywords.
     * 
     * DD-06 Optimization: 
     * - Customer's query is tokenized (simple local operation)
     * - Search is executed against pre-indexed branch keywords (no external calls)
     * - Results are ranked by TF-IDF
     * - Performance: 50-500ms (guarantees QAS-03: 3 seconds)
     * 
     * @param query Customer's natural language query (e.g., "깨끗한 헬스장")
     * @param customerId Customer identifier for preference tracking
     * @return Ranked list of branch recommendations
     */
    @Override
    public List<BranchRecommendation> searchBranches(SearchQuery query, Long customerId) {
        // Step 1: Tokenize query locally (no external calls)
        List<String> queryKeywords = queryTokenizer.tokenize(query.getText());
        
        // Step 2: Persist customer keywords for future personalization
        indexStage.persistCustomerKeywords(customerId, queryKeywords);
        
        // Step 3: Execute search directly against pre-indexed engine
        // This is the main optimization: no LLM call in Hot Path
        return branchPreferenceIndex.queryByKeywords(queryKeywords);
    }

    /**
     * Cold Path: Async pre-indexing of branch content.
     * 
     * DD-06 Implementation:
     * - Calls external LLM for keyword extraction (slow, outside Hot Path)
     * - Publishes event to PreferenceMatchConsumer
     * - Consumer is executed by PreferenceMatchScheduler during off-peak hours (DD-07)
     * - Indexing is done asynchronously, not blocking the API response
     * 
     * @param content Branch content (description, reviews, etc.)
     * @param sourceId Branch identifier
     * @param type Content type (DESCRIPTION, REVIEW, etc.)
     */
    @Override
    public void registerContent(String content, Long sourceId, ContentType type) {
        // Step 1: Extract keywords using external LLM (slow)
        List<String> preferenceKeywords = llmStage.extractKeywords(content);
        
        // Step 2: Persist to database
        indexStage.persistBranchKeywords(sourceId, preferenceKeywords);
        
        // Step 3: Publish event for async indexing via PreferenceMatchConsumer
        messagePublisher.publishEvent("branch.preferences.created", 
            new BranchPreferenceCreatedEvent(sourceId, preferenceKeywords));
    }
}
