package com.logica.ngph.esb.services;

import com.logica.ngph.common.dtos.NgphCanonical;

/**
 * @author guptarb
 *
 * This Inteface is used to Integrate with QLM Apps.
 */
public interface LmLinkService {

	public void doProcess(NgphCanonical ngphCanonical)throws Exception;
}
