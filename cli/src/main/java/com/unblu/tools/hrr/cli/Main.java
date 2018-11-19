package com.unblu.tools.hrr.cli;

import picocli.CommandLine;

public class Main {
	public static void main(String... args) {
		CommandLine.call(new RunServer(), args);
	}
}
