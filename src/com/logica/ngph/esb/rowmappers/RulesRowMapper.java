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
public class RulesRowMapper implements RowMapper<Rules>
{

	public Rules mapRow(ResultSet resultSet, int arg1) throws SQLException {
		Rules rule = new Rules();
		rule.setRuleAction(resultSet.getString("RULE_ACTION"));
		rule.setRuleActParam(resultSet.getString("RULE_ACT_PARAM"));
		rule.setRuleBranch(resultSet.getString("RULE_BRANCH"));
		rule.setRuleCategory(resultSet.getString("RULE_CATEGORY"));
		rule.setRuleCondition(resultSet.getString("RULE_CONDITION"));
		rule.setRuleDept(resultSet.getString("RULE_DEPT"));
		rule.setRuleDesc(resultSet.getString("RULE_DESCRIPTION"));
		rule.setRuleId(resultSet.getString("RULE_ID"));
		rule.setRuleMsgType(resultSet.getString("RULE_MSGTYPE"));
		rule.setRuleType(resultSet.getString("RULE_TYPE"));
		
		return rule;
	}

}
