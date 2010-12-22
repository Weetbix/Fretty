import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

//The batching/megastacks panel seen in the 2d / 3d deconvolution
public class BatchingPanel extends JPanel
{
	public BatchingPanel()
	{
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout( new GridLayout( 2, 1 ) );
		leftPanel.add( new JButton("Build Mega Stack") );
		leftPanel.add( new JCheckBox("Include megastacks") );

		JPanel wildcardPanel = new JPanel();
			wildcardPanel.setBorder( new TitledBorder("Stack names (wildcard)") );
			wildcardPanel.setLayout( new GridLayout(1,1) );
			wildcardPanel.add( new JTextField(15) );

		add( leftPanel );
		add( wildcardPanel );
	}
}
