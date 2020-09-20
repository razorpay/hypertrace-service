package org.hypertrace.core.query.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.hypertrace.core.query.service.api.QueryRequest;
import org.junit.jupiter.api.Test;

class RequestHandlerSelectorTest {

  @Test
  public void testHandlerSelection() {
    QueryRequest matchingQuery = QueryRequest.getDefaultInstance();
    QueryRequest otherQuery = QueryRequest.newBuilder().setLimit(10).build();
    ExecutionContext mockContext = mock(ExecutionContext.class);
    RequestHandlerRegistry mockRegistry = mock(RequestHandlerRegistry.class);
    RequestHandler<QueryRequest, ?> mockMatchingHandler = mock(RequestHandler.class);
    when(mockMatchingHandler.canHandle(matchingQuery, mockContext)).thenReturn(new QueryCost(0.5));
    when(mockMatchingHandler.canHandle(otherQuery, mockContext)).thenReturn(new QueryCost(-1));
    RequestHandler<QueryRequest, ?> mockNonMatchingHandler = mock(RequestHandler.class);
    when(mockNonMatchingHandler.canHandle(any(), any())).thenReturn(new QueryCost(-1));
    when(mockRegistry.getAll()).thenReturn(Set.of(mockMatchingHandler, mockNonMatchingHandler));

    RequestHandlerSelector selector = new RequestHandlerSelector(mockRegistry);

    assertEquals(mockMatchingHandler, selector.select(matchingQuery, mockContext));

    assertNull(selector.select(otherQuery, mockContext));
  }
}
