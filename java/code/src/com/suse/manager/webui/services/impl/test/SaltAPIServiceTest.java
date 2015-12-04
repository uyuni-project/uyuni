package com.suse.manager.webui.services.impl.test;


import java.util.Map;

import com.suse.manager.webui.services.impl.SaltAPIService;

import junit.framework.TestCase;

public class SaltAPIServiceTest extends TestCase {

	public void testGetGrain() {
		Map<String, Object> grains = SaltAPIService.INSTANCE.getGrains("suma3pg.vagrant.local");
		System.out.println("size:" + grains.size());
		for(Map.Entry<String, Object> grain: grains.entrySet()) {
			if(grain.getKey().equals("fqdn")) {
				System.out.println(grain.getKey() + " " + grain.getValue());
			}
		}
		
	}
	
}
