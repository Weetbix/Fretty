import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.plugin.filter.*;

public class Background_Reduction implements PlugInFilter 
{
	private final static int requirements =   DOES_STACKS 
						| ROI_REQUIRED 
						| DOES_ALL;

	private ImagePlus imp;

	public int setup( String arg, ImagePlus imp )
	{
		this.imp = imp;
		return requirements;			
	}

	public void run( ImageProcessor ip )
	{
		ImageStatistics stats = ip.getStatistics();

		ip.resetRoi();
		ip.add( -stats.mean );
	}

}
