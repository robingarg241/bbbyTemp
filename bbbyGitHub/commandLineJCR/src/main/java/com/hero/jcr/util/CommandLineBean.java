package com.hero.jcr.util;

/**
 * This class handles the programs arguments.
 */

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * This class handles the programs arguments.
 */
public class CommandLineBean {

	@Option(name = "--repolocation", aliases = { "-r" }, required = true,
            usage = "URL For Repo")
    
    private String repoLocation= null;

	@Option(name = "--username", aliases = { "-u" }, required = true,
            usage = "Repo User")
    
    private String username = null;
	
	@Option(name = "--password", aliases = { "-p" }, required = true,
            usage = "Repo Password")
    
    private String password = null;
	
	@Option(name = "--filename", aliases = { "-f" }, required = false,
            usage = "Input file name")
    
    private String filename = null;

	@Option(name = "--targeturl", aliases = { "-t" }, required = true,
            usage = "Target URL, this page should contain one and only one component")
    
    private String targeturl = null;
	
	private boolean errorFree = false;

    public CommandLineBean(String... args) {

    	CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(80);
        try {
         
        	parser.parseArgument(args);
            errorFree = true;
        
        } catch (CmdLineException e) {
        
        	System.err.println(e.getMessage());
            parser.printUsage(System.err);
        
        }
    }

    /**
     * Returns whether the parameters could be parsed without an
     * error.
     *
     * @return true if no error occurred.
     */
    public boolean isErrorFree() {
        return errorFree;
    }

	public String getRepoLocation() {
		return repoLocation;
	}
	
	public void setRepoLocation(String repoLocation) {
		this.repoLocation = repoLocation;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public void setTargeturl(String url) {
		this.targeturl = filename;
	}
	
	public String getTargeturl() {
		return targeturl;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public static void main (String[] args) {
	    
		CommandLineBean cli = new CommandLineBean(args);
	    
	    System.out.println(cli.repoLocation);
	    System.out.println(cli.username);
	    System.out.println(cli.password);
	    System.out.println(cli.targeturl);
	    
	  }

  
}
