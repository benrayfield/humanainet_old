/** Ben F Rayfield offers this mnistocrdataset dataset translator as public domain */
package humanainet.datasetsForAI.mnistocrdataset;

public class MnistLabeledImage{
	
	public final byte pixels[][];
	
	public final byte label;
	
	public MnistLabeledImage(byte pixels[][], byte label){
		this.pixels = pixels;
		this.label = label;
	}

}
