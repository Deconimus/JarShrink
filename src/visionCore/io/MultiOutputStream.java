package visionCore.io;

import java.io.IOException;
import java.io.OutputStream;

public class MultiOutputStream extends OutputStream {

	private final OutputStream[] streams;
	
	public MultiOutputStream(OutputStream... outs) {
		
		streams = new OutputStream[outs.length];
		
		for (int i = 0; i < outs.length; i++) {
			
			streams[i] = outs[i];
		}
		
	}
	
	@Override
	public void write(int b) throws IOException {
		
		for (OutputStream out : streams) {
			
			out.write(b);
		}
		
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		
		for (OutputStream out : streams) {
			
			out.write(b);
		}
		
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		
		for (OutputStream out : streams) {
			
			out.write(b, off, len);
		}
		
	}
	
	@Override
	public void flush() throws IOException {
		
		for (OutputStream out : streams) {
			
			out.flush();
		}
		
	}
	
	@Override
	public void close() throws IOException {
		
		for (OutputStream out : streams) {
			
			out.close();
		}
		
	}
	
}
