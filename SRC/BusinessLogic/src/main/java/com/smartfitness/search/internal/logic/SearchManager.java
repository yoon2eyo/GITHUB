package com.smartfitness.search.internal.logic;

import com.smartfitness.event.BranchPreferenceCreatedEvent;
import com.smartfitness.messaging.IMessagePublisherService;
import com.smartfitness.search.internal.pipeline.LlmKeywordExtractionStage;
import com.smartfitness.search.internal.pipeline.MatchIndexUpdateStage;
import com.smartfitness.search.model.BranchRecommendation;
import com.smartfitness.search.model.ContentType;
import com.smartfitness.search.model.SearchQuery;
import com.smartfitness.search.ports.ILLMAnalysisService;
import com.smartfitness.search.ports.ISearchRepository;
import com.smartfitness.search.ports.ISearchServiceApi;
import java.util.List;

/**
 * SearchManager: Orchestrates LLM analysis, preference persistence, and fast matching.
 */
public class SearchManager implements ISearchServiceApi {
    private final ISearchRepository repository;
    private final LlmKeywordExtractionStage llmStage;
    private final MatchIndexUpdateStage indexStage;
    private final IMessagePublisherService messagePublisher;

    public SearchManager(ISearchRepository repository,
                         ILLMAnalysisService llmClient,
                         IMessagePublisherService messagePublisher) {
        this.repository = repository;
        this.llmStage = new LlmKeywordExtractionStage(llmClient);
        this.indexStage = new MatchIndexUpdateStage(repository);
        this.messagePublisher = messagePublisher;
    }

    @Override
    public List<BranchRecommendation> searchBranches(SearchQuery query, Long customerId) {
        List<String> customerKeywords = llmStage.extractKeywords(query.getText());
        indexStage.persistCustomerKeywords(customerId, customerKeywords);
        return repository.executeMatchQuery(customerKeywords);
    }

    @Override
    public void registerContent(String content, Long sourceId, ContentType type) {
        List<String> preferenceKeywords = llmStage.extractKeywords(content);
        indexStage.persistBranchKeywords(sourceId, preferenceKeywords);
        messagePublisher.publishEvent("preferences", new BranchPreferenceCreatedEvent(sourceId, preferenceKeywords));
}
}
