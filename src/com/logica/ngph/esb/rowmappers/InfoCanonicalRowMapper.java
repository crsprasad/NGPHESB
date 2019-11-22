/**
 * 
 */
package com.logica.ngph.esb.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.logica.ngph.common.dtos.InfoCanonical;

/**
 * @author chakkar
 *
 */
public class InfoCanonicalRowMapper implements RowMapper<InfoCanonical> {

	public InfoCanonical mapRow(ResultSet resultSet, int arg1) throws SQLException {
		InfoCanonical infoCanonical = new InfoCanonical();
		infoCanonical.setBranch(resultSet.getString("MSGS_BRANCH"));
		infoCanonical.setDept(resultSet.getString("MSGS_DEPT"));
		infoCanonical.setDirection(resultSet.getString("MSGS_DIRECTION"));
		infoCanonical.setDstChnl(resultSet.getString("MSGS_DST_CHNL"));
		infoCanonical.setDstMsgSubType(resultSet.getString("MSGS_DST_MSGSUBTYPE"));
		infoCanonical.setDstMsgType(resultSet.getString("MSGS_DST_MSGTYPE"));
		infoCanonical.setEi_ID(resultSet.getString("MSGS_EI_ID"));
		infoCanonical.setInfo(resultSet.getString("MSGS_INFORMATION"));
		infoCanonical.setInstdagt_bkcd(resultSet.getString("MSGS_INSTDAGT_BKCD"));
		infoCanonical.setInstgagt_bkcd(resultSet.getString("MSGS_INSTGAGT_BKCD"));
		infoCanonical.setLstModTime(resultSet.getTimestamp("MSGS_LASTMODIFIEDTIME"));
		infoCanonical.setMsgMur(resultSet.getString("MSGS_MUR"));
		infoCanonical.setMsgRef(resultSet.getString("MSGS_MSGREF"));
		infoCanonical.setMsgStatus(resultSet.getString("MSGS_MSGSTS"));
		infoCanonical.setPmtId_instrId(resultSet.getString("MSGS_PMTID_INSTRID"));
		infoCanonical.setPmtId_relRef(resultSet.getString("MSGS_PMTID_RELREF"));
		infoCanonical.setSeqNo(resultSet.getString("MSGS_SEQNO"));
		infoCanonical.setSndrPymtPriority(resultSet.getString("MSGS_SNDRPYMTPRIORITY"));
		infoCanonical.setSrcMsgSubType(resultSet.getString("MSGS_SRC_MSGSUBTYPE"));
		infoCanonical.setSrcMsgType(resultSet.getString("MSGS_SRC_MSGTYPE"));
		return infoCanonical;
	}

}
