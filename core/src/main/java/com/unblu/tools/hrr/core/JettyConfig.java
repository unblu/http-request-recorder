package com.unblu.tools.hrr.core;

import java.util.Optional;

public class JettyConfig {
	private final Integer port;
	
	public JettyConfig(Integer port) {
		this.port = port;
	}
	
	public Optional<Integer> getPort() {
		return Optional.ofNullable(port);
	}
	
	public static JettyConfig defaultConfig() {
		return new JettyConfig(null);
	}
}
