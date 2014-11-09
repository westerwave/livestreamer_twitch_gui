package twitchlsgui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PromptReader implements Runnable {

    private InputStream inputStream;

    public PromptReader(InputStream inStream) {
	this.inputStream = inStream;
    }

    @Override
    public void run() {
	BufferedReader stdInput = new BufferedReader(new InputStreamReader(
		inputStream), 8 * 1024);
	String s = null;
	try {
	    while ((s = stdInput.readLine()) != null)
		System.out.println(s);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}