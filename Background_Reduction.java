import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.plugin.filter.*;

//Note: Only does 16 bit grayscale images for now. Because the imagej image types return 
//	different array types which you must cast to your own time. I'm going to code a plugin
//	which changes all the images to one generic type, maybe a float image. 

public class Background_Reduction implements PlugInFilter 
{
	private final static int requirements = 	DOES_ALL  
						| DOES_STACKS 
						| ROI_REQUIRED 
						| DOES_16;

	private ImagePlus imp;

	public int setup( String arg, ImagePlus imp )
	{
		this.imp = imp;
		return requirements;			
	}

	public void run( ImageProcessor ip )
	{
		short[] pixels = (short[]) ip.getPixels();
		Rectangle selection = ip.getRoi();

		//Find the mean value of all the pixels in the selection
		long total = 0;
		for( int y = selection.y; y < selection.y + selection.height; y++ )
		{
			//because pixels are stored as a 1d array
			int offset = y * ip.getWidth();

			for( int x = selection.x; x < selection.x + selection.width; x++ )
			{
				//javas shorts are all signed, so use binary AND to get rid 
				//of the signed bits before we add to the total.... Really high numbers will be negative 
				total += (long) (pixels[ offset + x ] & 0xFFFF);
			}
		}
		//calculate the actual mean
		total = ( total / (selection.width*selection.height));
		short average = (short) total;

		//Remove the background values from the image
		for( int i = 0; i < ip.getWidth() * ip.getHeight(); i++ )
		{
			if( pixels[i] > 0 ) 
			{
				pixels[ i ] -= average;
			
				//if the pixel value is below 0, it will be -1 etc. And ImageJ doesn't like this
				//because it seems to treat the signed short as unsigned when dispalying so
				//-1 becomes 65000 etc (white)
				f( pixels[i] < 0 ) pixels[i] = 0;
			}
			else
			{
				//The pixels value is negative, so add the average instead of deducting it..
				pixels[i] += average;
				//If the pixel was negative before, and has crossed into +, then it the real
				//pixel value is somewhere close to Short.MAX_VALUE - NOT 0!
				if( pixels[i] > 0 ) pixels[i] = Short.MAX_VALUE - pixels[i]
			}
		}

		imp.updateAndDraw();
	}



}
