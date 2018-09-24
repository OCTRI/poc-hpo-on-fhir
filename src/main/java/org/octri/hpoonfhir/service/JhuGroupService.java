package org.octri.hpoonfhir.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class JhuGroupService {
	
	// Map from the JHU Lab Id to a map representing the selection criteria
	Map<Long, Map<String,String>> asthmaGroupMap = new HashMap<>();
	
	public JhuGroupService() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream stream = classLoader.getResourceAsStream("json/JHU-Asthma-Groups.csv");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		int i = 1;
		String line = reader.readLine();
		while (line != null) {
			Map<String,String> group = new HashMap<>();
			String[] elements = line.split(",");
			group.put("gender", elements[1]);
			group.put("race", elements[2]);
			group.put("age", elements[3]);
			asthmaGroupMap.put(new Long(i), group);
			i++;
			line = reader.readLine();
		};
	}
	
	public Map<String,String> getAsthmaGroupForId(Long id) {
		return asthmaGroupMap.get(id);
	}

}
