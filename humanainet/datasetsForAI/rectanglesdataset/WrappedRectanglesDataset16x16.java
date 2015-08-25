/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.datasetsForAI.rectanglesdataset;
import humanainet.wavetree.bit.AllOne;
import humanainet.wavetree.bit.AllZero;
import humanainet.wavetree.bit.Bits;
import humanainet.wavetree.bit.Fast0To16Bits;
import humanainet.wavetree.bit.object.Polydim;
import humanainet.wavetree.bit.object.SimplePolydim;

/** A dataset designed for easy testing of learning algorithms
based on which things overlap eachother and how much.
*/
public class WrappedRectanglesDataset16x16{
	private WrappedRectanglesDataset16x16(){}
	
	
	public static Polydim dataset4OnRowsEach(){
		Bits blackRow = new AllZero(16);
		Bits whiteRow = new AllOne(16);
		Bits b = Fast0To16Bits.EMPTY;
		for(int whichImage=0; whichImage<16; whichImage++){
			boolean rows[] = new boolean[16];
			int firstRow = whichImage;
			for(int rowOffset=0; rowOffset<4; rowOffset++){
				rows[(firstRow+rowOffset)%rows.length] = true;
			}
			for(int r=0; r<rows.length; r++){
				b = b.cat(rows[r] ? whiteRow : blackRow);
			}
		}
		return new SimplePolydim(b, 16, 16, 16);
	}
	
	public static Polydim datasetRectFromEachPoint(int eachRectWidth, int eachRectHeight){
		Bits b = Fast0To16Bits.EMPTY;
		for(int startY=0; startY<16; startY++){
			for(int startX=0; startX<16; startX++){
				boolean points[] = new boolean[256];
				for(int plusY=0; plusY<eachRectHeight; plusY++){
					for(int plusX=0; plusX<eachRectWidth; plusX++){
						int y = (startY+plusY)%16;
						int x = (startX+plusX)%16;
						points[y*16+x] = true;
					}
				}
				for(int i=0; i<256; i++){
					b = b.cat(points[i] ? Fast0To16Bits.TRUE : Fast0To16Bits.FALSE);
				}
			}
		}
		return new SimplePolydim(b, 256, 16, 16);
	}
	

}
