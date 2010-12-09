import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.awt.event.*;
import ij.plugin.frame.*;

import javax.swing.*;
import javax.swing.border.*;

import javax.*;
import javax.swing.plaf.*;

class FrettyCommonPanel extends JPanel 
{
	public FrettyCommonPanel()
	{
		setBorder( new TitledBorder("Common") );
		add( new Button("Open ROI Manager") );
		add( new Button("Background Reduction" ) );
	}
}

class FrettyReferenceSpectraPanel extends JPanel
{
	public FrettyReferenceSpectraPanel()
	{
		setBorder( new TitledBorder("Reference Spectra") );
		
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) ); 
		add( new FrettySpectraSelector() );
		add( new FrettySpectraSelector() );
		add( new FrettySpectraSelector() );
	}
}

class FrettySpectraSelector extends JPanel
{
	Label label = new Label("SDD");
	Button load = new Button( "Load" );
	Button create = new Button( "Create" );
	Button save = new Button( "Save" );
	Button view = new Button( "View" );

	public FrettySpectraSelector()
	{	
		add( label );
		add( load );
		add( create );
		add( save );
		add( view );

		setEnabled( false );
	}

	public void setEnabled( boolean b ) 
	{
		label.setEnabled( b );
		load.setEnabled( b );
		create.setEnabled( b );
		save.setEnabled( b );
		view.setEnabled( b );
	}
}

class FrettyFRETSamplesPanel extends JPanel
{
	public FrettyFRETSamplesPanel()
	{
		setBorder( new TitledBorder("FRET Samples") );
		setLayout( new BoxLayout( this, BoxLayout.X_AXIS) ); 
	
		Panel p1 = new Panel();
		p1.add( new Button("Build Mega Stack") );
		p1.add( new TextField(15) );

		add(p1);
		
		add( new Button("dfdf") );	
	}
}

public class Fretty extends PlugInFrame 
{
	public Fretty () 
	{
		super("Fretty");

		setLayout( new BoxLayout(this, BoxLayout.Y_AXIS)) ;

		/////////////////////////////////////////////////////////
		//Setup gui and callbacks
		////////////////////////////////////////////////////////

		//We need swing for the panel borders, so make it look like the OS items
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e ){};

		JPanel p = new FrettyCommonPanel();
		add(p);

		add( new FrettyReferenceSpectraPanel() );
		add( new FrettyFRETSamplesPanel() );
		pack();
		GUI.center(this);
		setVisible( true );
	}
}
