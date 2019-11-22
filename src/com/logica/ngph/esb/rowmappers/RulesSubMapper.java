package com.logica.ngph.esb.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.logica.ngph.esb.Dtos.Rules;

/**
 * 
 * @author mohdabdulaa
 *
 */
public class RulesSubMapper implements RowMapper<Rules> {

	public Rules mapRow(ResultSet resultSet, int arg1) throws SQLException {
		Rules rule = new Rules();
		rule.setRuleId(resultSet.getString("RULE_ID"));
		rule.setRuleAction(resultSet.getString("RULE_ACTION"));
		rule.setRuleActParam(resultSet.getString("RULE_ACT_PARAM"));
		rule.setRuleCondition(resultSet.getString("RULE_SYS_CONDITION"));
		rule.setRuleType(resultSet.getString("RULE_TYPE"));

		return rule;
	}

}
