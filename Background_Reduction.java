import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.plugin.filter.*;

//Note: Only does 32 bit grayscale images for now. Because the imagej image types return 
//	different array types which you must cast to your own time. I'm going to code a plugin
//	which changes all the images to one generic type, maybe a float image. 

public class Background_Reduction implements PlugInFilter 
{
	private final static int requirements =   DOES_STACKS 
						| ROI_REQUIRED 
						| DOES_32;

	private ImagePlus imp;

	public int setup( String arg, ImagePlus imp )
	{
		this.imp = imp;
		return requirements;			
	}

	public void run( ImageProcessor ip )
	{
		float[] pixels = (float[]) ip.getPixels();
		Rectangle selection = ip.getRoi();
		ip.resetRoi();					//Act on the whole image..

		//Find the mean value of all the pixels in the selection
		long total = 0;
		for( int y = selection.y; y < selection.y + selection.height; y++ )
		{
			//because pixels are stored as a 1d array
			int offset = y * ip.getWidth();

			for( int x = selection.x; x < selection.x + selection.width; x++ )
			{
				total += pixels[ offset + x ];
			}
		}
		//calculate the actual mean
		int average = (int) ( total / (selection.width*selection.height));

		//Remove the average value from all pixels 
		ip.add( -average );
		
		//set all pixels less than 0 to 0, only needed for the 32-bit version. 
		ip.min( 0 );

		imp.updateAndDraw();
	}

}
