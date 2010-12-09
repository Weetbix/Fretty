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

//The panel at the top of the gui screen...
class FrettyTopPanel extends JPanel
{
	//Padding
	private static final Insets insets = new Insets( 10,10,10,10 );
	public Insets getInsets() { return insets; }

	public FrettyTopPanel()
	{
		setLayout( new GridLayout( 4, 2 ) );
		add( new JLabel( "Cross Excitation Correction" ) );
		JCheckBox crossCorrection = new JCheckBox();
		add( crossCorrection );

		add( new JLabel("Wavelengths Per Sample  " ) );
		JSpinner wavelengthsPerSample = new JSpinner(
					new SpinnerNumberModel( 25, 1, 9999, 1 ) );
		add( wavelengthsPerSample );

		add( new JLabel( "Donor quantum yield" ) );
		JSpinner donorQuantumYield = new JSpinner( 
					new SpinnerNumberModel( (double)150, 0, 40000, 0.001 ) );
		add( donorQuantumYield );

		add( new JLabel( "Acceptor quantum yield" ) );
		JSpinner acceptorQuantumYield = new JSpinner( 
					new SpinnerNumberModel( (double)150, 0, 40000, 0.001 ) );
		add( acceptorQuantumYield );
	}
}

class FrettyCommonPanel extends JPanel 
{
	public FrettyCommonPanel()
	{
		setBorder( new TitledBorder("Common Tools") );
		add( new JButton("Open ROI Manager") );
		add( new JButton("Background Reduction" ) );
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
	JLabel label = new JLabel("SDD");
	JButton load = new JButton( "Load" );
	JButton create = new JButton( "Create" );
	JButton save = new JButton( "Save" );
	JButton view = new JButton( "View" );

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
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS) ); 
	
		JPanel p1 = new JPanel();
		p1.add( new JButton("Build Mega Stack") );
		p1.add( new JTextField(15) );
		add(p1);

		add( new JCheckBox("Include megastacks") );

		add( new JButton("Indicate donor excitation stack") );
		add( new JButton("Indicate acceptor excitation stack" ) );
		
		//add( new Button("dfdf") );	
	}
}

public class Fretty_ extends PlugInFrame 
{
	public Fretty_ () 
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

		add( new FrettyTopPanel() );
		add( new FrettyCommonPanel() );
		add( new FrettyReferenceSpectraPanel() );
		add( new FrettyFRETSamplesPanel() );

		pack();
		GUI.center(this);
		setVisible( true );
	}
}