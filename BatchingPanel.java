import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

//The batching/megastacks panel seen in the 2d / 3d deconvolution
public class BatchingPanel extends JPanel
{
	public BatchingPanel()
	{
		add( new JButton("Build Mega Stack") );

		JPanel wildcardPanel = new JPanel();
			wildcardPanel.setLayout( new GridLayout(2,1) );
			wildcardPanel.setBorder( new TitledBorder("Stack names (wildcard)") );
		
			wildcardPanel.add( new JTextField(15) );
			wildcardPanel.add( new JCheckBox("Include megastacks") );

		add( wildcardPanel );
	}
}
