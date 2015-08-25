package humanainet.blackholecortex.ui;
import humanainet.wavetree.bit.Bits;
import humanainet.wavetree.bit.object.Polydim;
import humanainet.blackholecortex.WeightsNode;
import humanainet.blackholecortex.anneal.AnnealStrategy;
import humanainet.blackholecortex.anneal.SimpleAnnealStrategy;
import humanainet.blackholecortex.boltz.RbmData;
import humanainet.blackholecortex.boltz.RbmLayer;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.FlatPYX;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.FlatYX;
import humanainet.blackholecortex.neuralshapes_TODOReorganizeAndRemoveMuchOfThis.ObservedRect;
import humanainet.common.CoreUtil;
import humanainet.common.Nanotimer;
import humanainet.datasetsForAI.mnistocrdataset.MnistLabeledImage;
import humanainet.datasetsForAI.mnistocrdataset.MnistOcrDataset;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

/** TODO building rbm visually as Rectangles here. Separate that logic from display and data. */
public class ResizGraphWithMnist extends NodeScreen{
	
	//public final MnistLabeledImage /*trainingImages[],*/ testImages[];
	
	/** Reduce mnistOcrDataset from 28x28 images with scalar brightness and 1 byte label,
	to 16x16 images, where middle 14x14 are half size in each dim of 28x28 and
	the first 10 bitvars are which digit, or maybe it should be 2 rows of 5 bitvars
	and put the 14x14 grid in the lower right corner (yes I'll use that dataFormat).
	All layer sizes will be a multiple of 16 so they fit together on screen.
	Each Bits object in this List is size 256.
	Read it as 1 javaclass://wavetree.bit.ByteArrayUntilSplit per image.
	<br><br>
	I want that mnistOcrDataset converted to that data format, 256 bits per image with label,
	in a simple array of bits, so it will load faster. I cant have it taking 5 extra seconds
	to load each time, even for the smaller testing dataset, when I'm not using its scalars.
	*/
	public final Polydim mainDataset;
	//public final List<boolean[]> mainDataset;
	//TODO public final List<Bits> mainDataset = TODO;
	//TODO? See BhcDatastructUtil public static final List<Bits> mainDataset =
	//What training do I want it to do? I want to explore
	
	protected long clicks;
	
	protected String textToPaint = "";
	
	public final RbmData mainRBM;

	public ResizGraphWithMnist(FlatPYX data){
		super(data);
		//trainingImages = MnistOcrDataset.readTrainingLabeledImages();
		//testImages = MnistOcrDataset.readTestLabeledImages();
		//TODO use main images to train, not from the test dataset, but I like its smaller size for now
		//mainDataset = MnistOcrDataset.readTestLabeledImages16x16();
		Nanotimer t = new Nanotimer();
		mainDataset = MnistOcrDataset.readTestLabeledImages16x16AsMultiDim();
		double readMnistDataDuration = t.secondsSinceLastCall();
		System.out.println("readTestLabeledImages16x16AsMultiDim took "+readMnistDataDuration+" seconds.");
		displayWhatInEachColor[0] = DisplayWhat.Nothing; //red's default
		displayWhatInEachColor[1] = DisplayWhat.Nothing; //green's default
		displayWhatInEachColor[2] = DisplayWhat.ChanceOrInfluenceOfThisNode; //blue's default
		int yBlockSize = 16;
		AnnealStrategy anneal = new SimpleAnnealStrategy(30, 2, 1);
		int startX = 0, startY = 0;
		mainRBM = new RbmData(
			startX,
			startY,
			data,
			yBlockSize,
			new int[]{
				yBlockSize*16,
				yBlockSize*24,
				yBlockSize*32,
				yBlockSize*40
			},
			new int[]{
				yBlockSize*5, //TODO after rbm is working without extra layers hanging off
				yBlockSize*5,
				yBlockSize*5
			}
			//anneal
		);
		/*TODO commented this because code changed.
		for(ObservedRect memRect : mainRBM.shortTermMemoryLayers){ //test display
			for(Neuron node : nodesCacheable(memRect.rect)){
				node.influence = (node.bit = true) ? 1 : 0;
			}
		}*/
	}
	
	public void mouseMoved(MouseEvent e){
		super.mouseMoved(e);
		clicks++;
		refreshMnistImage();
	}
	
	/** display next image at each click */
	public void mouseReleased(MouseEvent e){
		super.mouseReleased(e);
		//MnistLabeledImage image = trainingImages[(int)(clicks%trainingImages.length)];
		clicks++;
		refreshMnistImage();
	}
	
	protected void refreshMnistImage(){
		//observe(testImages[(int)(clicks%testImages.length)]);
		//observe(mainDataset.get((int)(clicks%mainDataset.size())));
		int whichImage = (int)(clicks%mainDataset.dimSize(0));
		observe(mainDataset.bits(whichImage));
	}
	
	protected void observe(Polydim image){
		if(image.dims() != 2 || image.dimSize(0) != 16 || image.dimSize(1) != 16){
			//TODO generalize this class to any size of image and not just mnistOcrDataset
			throw new RuntimeException("Wrong size image: "+image);
		}
		List<WeightsNode> visibleNodes = mainRBM.mainLayers[0].fromUp;
		Bits imageBits = image.data();
		//iterator is much faster than get(int) in ObservedRect kind of List<Neuron>,
		//but you can use any kind of List<Neuron>.
		long g = 0;
		for(WeightsNode n : visibleNodes){
			boolean pixel = imageBits.bitAt(g++);
			n.scalar = (n.bit = pixel) ? 1 : 0;
		}
		/*
		Rectangle layer = mainRBM.mainRbmLayers[0].rect;
		int sizeX = (int)CoreUtil.min(image.dimSize(0), data.xSize-layer.x);
		int sizeY = (int)CoreUtil.min(image.dimSize(0), data.xSize-layer.x);
		for(int x=0; x<sizeX; x++){
			for(int y=0; y<sizeY; y++){
				Neuron n = data.yx[y][x];
				boolean pixel = image.bit(x, y);
				n.influence = (n.bit = pixel) ? 1 : 0;
			}
		}*/
	}
	
	/*protected void observe(boolean image16x16[]){
		if(image16x16.length != 256) throw new IllegalArgumentException(
			"Bit array size is "+image16x16.length+" but must be 256 for 16x16");
		//int xStart = 0;
		//int yStart = (mainRbmLayers.length-1)*16; //below other rbm layers
		//int yStart = yStartOfLayer(0);
		Rectangle layer = mainRBM.mainRbmLayers[0].rect;
		for(int i=0; i<256; i++){
			int x = layer.x+i/16; //TODO swap x and y?
			int y = layer.y+i%16;
			Neuron n = data.yx[y][x];
			n.influence = (n.bit = image16x16[i]) ? 1 : 0;
		}
	}*/
	
	/*protected void observe(MnistLabeledImage image){
		textToPaint = "Digit "+image.label;
		int xEnd = CoreUtil.min(image.pixels[0].length, data.xSize);
		int yEnd = CoreUtil.min(image.pixels.length, data.ySize);
		for(int x=0; x<xEnd; x++){
			for(int y=0; y<yEnd y++){
				byte brightness = image.pixels[x][y];
				double brightnessFraction = brightness/255.;
				Neuron n = data.yx[y][x];
				n.influence = (n.bit=brightnessFraction==0) ? 0 : 1;
				//n.influence = Math.pow(brightnessFraction, 1/32.);
			}
		}
	}*/
	
	public void paint(Graphics g){
		super.paint(g);
		g.setColor(Color.white);
		g.drawString(textToPaint, 25, 25);
	}
	
	/*public Neuron[] nodesCacheable(Rectangle rect){
		return data.nodesCacheable(rect);
	}*/

}