package com.logica.ngph.validators.rowMapper;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.logica.ngph.validators.dto.MsgField;

public class MsgFieldRowMapper implements RowMapper<MsgField>,Serializable{

	static Logger logger = Logger.getLogger(MsgFieldRowMapper.class);

	private static final long serialVersionUID = 1L;
	private String fldNo;
	private String fldCompFmt;
	private String fldCompSeq;
	private String fldCompManOpt;
	
	// Extra Variables Required
	private boolean isSlash=false;
	private boolean isPling=false;
	private String charType="";
	private int lengthOfField=0;
	private int noOfLines=1;
	private String consideration;
	private String eoCompIndctr;
	private String fldCodeWrds;

	MsgField msgsPolled=null;
	
	private void doinstrumentation(String CompFormat)
	{
		isSlash=false;
		isPling=false;
		charType="";
		lengthOfField=0;
		noOfLines=1;
		
		char[] fieldValArr = CompFormat.toCharArray();
		for(int i=0;i<fieldValArr.length;i++)
		{
			//System.out.println(fldNo);
			if(fieldValArr[i]=='/')
			{
				this.isSlash=true;
			}
			else if(fieldValArr[i]=='!')
			{
				this.isPling=true;
			}
			else if(fieldValArr[i]=='a'|| fieldValArr[i]=='c'||fieldValArr[i]=='d'||fieldValArr[i]=='n'||fieldValArr[i]=='z' ||fieldValArr[i]=='x')
			{
				this.charType=fieldValArr[i]+"";
			}
			else if(fieldValArr[i]=='*')
			{
				this.noOfLines = lengthOfField;
				this.lengthOfField=0;
			} 
			else
			{
				this.lengthOfField = (10*lengthOfField)+Integer.parseInt(fieldValArr[i]+"");
			}
			
			//System.out.println(lengthOfField);
	
			
		}
		
	}
	public MsgField mapRow(ResultSet resultSet, int arg1) throws SQLException {
		
		msgsPolled = new MsgField();
	
		/*
		 * FIELD_NO
		 * FIELD_COMP_FORMAT
		 * FIELD_COMP_SEQ
		 * FIELD_COMP_MANDOPT
		 * 
		 */
		isSlash=false;
		isPling=false;
		charType="";
		lengthOfField=0;
		noOfLines=1;
		fldNo=resultSet.getString("FIELD_NO");
		fldCompFmt = resultSet.getString("FIELD_COMP_FORMAT");
		fldCompSeq= resultSet.getString("FIELD_COMP_SEQ");
		fldCompManOpt= resultSet.getString("FIELD_COMP_MANDOPT");
		consideration= resultSet.getString("FIELD_CONSIDERATION");
		eoCompIndctr= resultSet.getString("FIELD_EOC_IND");
		fldCodeWrds = resultSet.getString("FIELD_CODEWORDS");
		
		//Condtion put for ISo Message as Iso message may have certain component format as null
		if(fldCompFmt!=null && StringUtils.isNotBlank(fldCompFmt)&& StringUtils.isNotEmpty(fldCompFmt))
		{
			//If the consideration is not there then format splitting is to be done but if consideration is there and it is anything other than bic or ifsc 
			//then too splitting is to be done, so these if conditions below 
			if (consideration!=null)
			{
				if (!consideration.equalsIgnoreCase("BIC") && !consideration.equalsIgnoreCase("IFS"))
				{
					doinstrumentation(fldCompFmt);
				}
			}
			else
			{
				doinstrumentation(fldCompFmt);
			}
		}
		
		msgsPolled.setFldNo(fldNo);
		msgsPolled.setFldCompFmt(fldCompFmt);
		msgsPolled.setFldCompSeq(fldCompSeq);
		msgsPolled.setFldCompManOpt(fldCompManOpt);
		msgsPolled.setSlash(isSlash);
		msgsPolled.setPling(isPling);
		msgsPolled.setCharType(charType);
		msgsPolled.setLengthOfField(lengthOfField);
		msgsPolled.setNoOfLines(noOfLines);
		msgsPolled.setEoCompIndctr(eoCompIndctr);
		msgsPolled.setConsideration(consideration);
		msgsPolled.setFldCodeWrds(fldCodeWrds);
		
		return msgsPolled;
	}


}
