import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.plugin.filter.*;

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
				//Javas shorts are always signed :( need to convert with a binary AND
				total += (long) (pixels[ offset + x ] & 0xFFFF);
			}
		}
		//calculate the actual mean
		total = ( total / (selection.width*selection.height));
		short average = (short) total;
		
		IJ.showMessage( "Some val", Short.toString( pixels[100] ) );
		//Remove the background values from the image
		for( int i = 0; i < ip.getWidth() * ip.getHeight(); i++ )
		{
		//	pixels[ i ] -= average;
		}

		pixels[100] = -1;
		IJ.showMessage("AFter..", Short.toString(pixels[100]) );

		IJ.showMessage( "Average:", Short.toString(average));

		imp.updateAndDraw();
	}



}
