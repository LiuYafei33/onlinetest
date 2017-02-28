package org.base.frame.dao.dialect;

import org.apache.commons.lang3.StringUtils;
import org.base.frame.util.Page;
import org.springframework.stereotype.Component;

@Component("postgresqlDialect")
public class PostgresqlDialect implements IDialect {

	@Override
	public String getPageSql(String sql, String orderby, Page page) {
		// 设置分页参数
		int pageSize = page.getPageSize();
		int pageNo = page.getPageIndex();
		StringBuffer sb = new StringBuffer(sql);
		if (StringUtils.isNotBlank(orderby)) {
			sb.append(" ").append(orderby);
		}
		sb.append(" limit ").append(pageSize).append(" offset ")
				.append(pageSize * (pageNo - 1));

		return sb.toString();
	}

	@Override
	public String getDataDaseType() {
		return "postgresql";
	}

	@Override
	public boolean isRowNumber() {
		return false;
	}

}
