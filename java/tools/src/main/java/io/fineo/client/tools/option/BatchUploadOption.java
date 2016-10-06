package io.fineo.client.tools.option;


import com.beust.jcommander.Parameter;

public class BatchUploadOption {

  @Parameter(names = {"-f", "--file"}, description = "Full path to the file to upload")
  public String file;

  @Parameter(names ={"-c", "--check-status"},
             description = "If we should check the state of the file")
  public boolean check = false;
}
