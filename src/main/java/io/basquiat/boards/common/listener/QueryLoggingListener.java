package io.basquiat.boards.common.listener;

import io.r2dbc.proxy.core.QueryExecutionInfo;
import io.r2dbc.proxy.listener.ProxyExecutionListener;
import io.r2dbc.proxy.support.QueryExecutionInfoFormatter;
import lombok.extern.slf4j.Slf4j;

/**
 * r2dbc proxy를 활용한 쿼리 로그
 */
@Slf4j
public class QueryLoggingListener implements ProxyExecutionListener {

    @Override
    public void afterQuery(QueryExecutionInfo execInfo) {
        QueryExecutionInfoFormatter formatter = new QueryExecutionInfoFormatter().addConsumer((info, sb) -> {
                                                                                        sb.append("ConnectionId: ");
                                                                                        sb.append(info.getConnectionInfo().getConnectionId());
                                                                                 })
                                                                                 .newLine()
                                                                                 .showQuery()
                                                                                 .newLine()
                                                                                 .showBindings()
                                                                                 .newLine();
        log.info(formatter.format(execInfo));
    }

}
