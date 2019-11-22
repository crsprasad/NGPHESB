package com.logica.ngph.esb.servicesImpl;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import com.logica.ngph.common.dtos.InfoCanonical;
import com.logica.ngph.common.utils.PropertyReader;
import com.logica.ngph.esb.constants.NgphEsbConstants;
import com.logica.ngph.esb.daos.EsbServiceDao;
import com.logica.ngph.esb.services.InfoCanonicalService;

public class InfoCanonicalServiceImpl implements InfoCanonicalService {

	private EsbServiceDao esbServiceDao;
	
	public void setEsbServiceDao(EsbServiceDao esbServiceDao) {
		this.esbServiceDao = esbServiceDao;
	}
	
	public InfoCanonical enrichInfoCanonical(InfoCanonical obj) throws Exception
	{
		//Set Branch based on receiver bank/BIC
		String branchCode = null;
		branchCode = esbServiceDao.findBranchCodeByBic(obj.getInstgagt_bkcd());
		
		if(StringUtils.isNotBlank(branchCode))
		{
			obj.setBranch(branchCode);
		}
		else
		{	//Set Default Branch
	       	obj.setDept(esbServiceDao.getInitialisedValue(NgphEsbConstants.INITIALISED_BRANCH_VALUE));
		}
		
		//set Default department
       	obj.setDept(esbServiceDao.getInitialisedValue(NgphEsbConstants.INITIALISED_DEPT_VALUE));  
		
        //Fetch the Destination Channel Type based on dstEiId	
		//set Ei_ID
		if(obj.getDirection().equalsIgnoreCase(NgphEsbConstants.INWARD_PAYMENT))
		{
			String dstChnlType = esbServiceDao.getDstChnlType(PropertyReader.getMapValue("INFO_EI_I"));
			obj.setDstChnl(dstChnlType);
			
			obj.setEi_ID(PropertyReader.getMapValue("INFO_EI_I"));
		}
		else
		{
			String dstChnlType = esbServiceDao.getDstChnlType(PropertyReader.getMapValue("INFO_EI_O"));
			obj.setDstChnl(dstChnlType);
			
			obj.setEi_ID(PropertyReader.getMapValue("INFO_EI_O"));
		}
		
		// Fetch dst Msg type and Dst Sub Msg type from Ta_Msgs_Mapping passing srcmsgtype and srcmsgsubtype and dst Chnl type
		List<String> dstMsgInfo = esbServiceDao.getDstMsgtype(obj.getSrcMsgType(), obj.getSrcMsgSubType(),obj.getDstChnl(),obj.getDirection());
		if(dstMsgInfo!=null && dstMsgInfo.size() > 0)
		{
			obj.setDstMsgType(dstMsgInfo.get(0));
			obj.setDstMsgSubType(dstMsgInfo.get(1));
		}
		return obj;
	}

}
