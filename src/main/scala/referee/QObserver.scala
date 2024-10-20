package referee

import common.GameState
import java.awt.event.{ActionEvent, ActionListener}
import java.awt.FlowLayout
import javax.swing.{ImageIcon, JFrame, JPanel, JLabel, JButton, JTextField, WindowConstants}
import scala.collection.mutable.ArrayBuffer
import java.awt.Image
import os._

/*
An observer that accumulates a queue of images to display in a GUI to the viewer.
*/
class QObserver extends Observer {
    private var states = ArrayBuffer[Image]()
    private var currStateIdx: Option[Int] = None
    private var currState: Option[GameState] = None

    var nextImagePrefix: Int = 0
    if (!(os.exists(os.root / "Tmp") && os.isDir(os.root / "Tmp"))) {
        os.makeDir(os.root / "Tmp")
    }

    override def receiveState(state: GameState): Unit = {
        saveStateImage(state, s"Tmp/$nextImagePrefix.png")
        nextImagePrefix += 1

        states += state.render
        currState = Some(state)
    }
        
    override def gameOver(result: GameResult): Unit = {
        states += result.render
        currStateIdx match {
            case None => currStateIdx = Some(0)
            case Some(value) =>
        }
    }

    /*
    Returns an Option containing the Image stored 'delta' indices from the current displayed state.
    If no state is currently being displayed, returns the front state if it exists.
    */
    def shiftState(delta: Int): Option[Image] = currStateIdx match {
        case None => {
            if (states.size > 0) {
                currStateIdx = Some(delta - 1)
                states.lift(delta - 1)
            } else None
        }
        case Some(value) => {
            currStateIdx = Some(value + delta)
            states.lift(value + delta)
        }
    }

    def getNextState: Option[Image] = shiftState(1)

    def getPrevState: Option[Image] = shiftState(-1)

    /*
    Saves the current state as a JSON to the provided filename.
    */
    def saveState(filename: String): Unit = currState match {
        case None => 
        case Some(value) => saveStateJSON(value, filename)
    }

    // Create and display GUI
    {
        val frame = new JFrame()
        frame.setLayout(new FlowLayout())

        val label = new JLabel()
        frame.add(label)

        val prev = new JButton("Previous")
        prev.setActionCommand("prev")
        frame.add(prev)

        val next = new JButton("Next")
        next.setActionCommand("next")
        frame.add(next)

        val save = new JButton("Save")
        save.setActionCommand("save")
        frame.add(save)

        val filenameField = new JTextField(10) 
        frame.add(filenameField)

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

        val actionListener = new ActionListener {
            override def actionPerformed(e: ActionEvent): Unit = e.getActionCommand() match {
                case "next" => getNextState match {
                    case None =>
                    case Some(state) => label.setIcon(new ImageIcon(state))
                }
                case "save" => saveState(filenameField.getText())
                case "prev" => getPrevState match {
                    case None => 
                    case Some(state) => label.setIcon(new ImageIcon(state))
                }
            }
        }

        next.addActionListener(actionListener)
        save.addActionListener(actionListener)
        prev.addActionListener(actionListener)

        frame.setVisible(true)
    }
}