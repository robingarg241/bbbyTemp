package com.bbby.aem.core.services;

import com.bbby.aem.core.api.PDMAPICommand;

import javax.jcr.Session;

public interface CallPDMService {
    int makePDMCall(Session session, String assetPath, PDMAPICommand pdmapiCommand) throws Exception;
}
