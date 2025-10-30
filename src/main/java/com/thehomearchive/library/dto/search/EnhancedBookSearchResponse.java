package com.thehomearchive.library.dto.search;

import com.thehomearchive.library.dto.book.BookResponse;
import com.thehomearchive.library.service.ExternalBookSearchService;

import java.util.List;

/**
 * Enhanced search response that includes both local and external API results.
 * Used for searches that may fallback to external APIs when local results are insufficient.
 */
public class EnhancedBookSearchResponse {

    private BookSearchPageResponse localResults;
    private List<BookResponse> externalResults;
    private boolean externalSearchPerformed;
    private ExternalBookSearchService.ExternalApiHealthStatus externalApiHealthStatus;
    private long totalSearchTimeMs;

    // Constructors
    public EnhancedBookSearchResponse() {
    }

    public EnhancedBookSearchResponse(BookSearchPageResponse localResults, 
                                    List<BookResponse> externalResults, 
                                    boolean externalSearchPerformed) {
        this.localResults = localResults;
        this.externalResults = externalResults;
        this.externalSearchPerformed = externalSearchPerformed;
    }

    // Getters and Setters
    public BookSearchPageResponse getLocalResults() {
        return localResults;
    }

    public void setLocalResults(BookSearchPageResponse localResults) {
        this.localResults = localResults;
    }

    public List<BookResponse> getExternalResults() {
        return externalResults;
    }

    public void setExternalResults(List<BookResponse> externalResults) {
        this.externalResults = externalResults;
    }

    public boolean isExternalSearchPerformed() {
        return externalSearchPerformed;
    }

    public void setExternalSearchPerformed(boolean externalSearchPerformed) {
        this.externalSearchPerformed = externalSearchPerformed;
    }

    public ExternalBookSearchService.ExternalApiHealthStatus getExternalApiHealthStatus() {
        return externalApiHealthStatus;
    }

    public void setExternalApiHealthStatus(ExternalBookSearchService.ExternalApiHealthStatus externalApiHealthStatus) {
        this.externalApiHealthStatus = externalApiHealthStatus;
    }

    public long getTotalSearchTimeMs() {
        return totalSearchTimeMs;
    }

    public void setTotalSearchTimeMs(long totalSearchTimeMs) {
        this.totalSearchTimeMs = totalSearchTimeMs;
    }

    // Convenience methods
    public int getTotalLocalResults() {
        return localResults != null ? localResults.getContent().size() : 0;
    }

    public int getTotalExternalResults() {
        return externalResults != null ? externalResults.size() : 0;
    }

    public int getTotalCombinedResults() {
        return getTotalLocalResults() + getTotalExternalResults();
    }

    public boolean hasExternalResults() {
        return externalResults != null && !externalResults.isEmpty();
    }

    public boolean hasLocalResults() {
        return localResults != null && localResults.getContent() != null && !localResults.getContent().isEmpty();
    }

    public boolean hasAnyResults() {
        return hasLocalResults() || hasExternalResults();
    }

    @Override
    public String toString() {
        return "EnhancedBookSearchResponse{" +
                "localResults=" + getTotalLocalResults() +
                ", externalResults=" + getTotalExternalResults() +
                ", externalSearchPerformed=" + externalSearchPerformed +
                ", totalSearchTimeMs=" + totalSearchTimeMs +
                '}';
    }
}