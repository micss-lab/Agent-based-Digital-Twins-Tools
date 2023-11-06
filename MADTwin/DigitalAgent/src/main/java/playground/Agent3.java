package playground;

import helpers.Delay;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Agent3 extends Agent {
    Logger logger = LoggerFactory.getLogger("Agent3");
    private jade.util.Logger log = jade.util.Logger.getMyLogger(getClass().getName());

    private ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
    private ThreadedBehaviourFactory tbf2 = new ThreadedBehaviourFactory();
    @Override
    protected void setup() {
//        ParallelBehaviour p= new ParallelBehaviour();
//        p.addSubBehaviour(tbf.wrap(cyclicBehaviour1));
//        p.addSubBehaviour(tbf2.wrap(cyclicBehaviour2));
//        p.addSubBehaviour(cyclicBehaviour3);
//        addBehaviour(cyclicBehaviour1);
//        addBehaviour(tbf.wrap(cyclicBehaviour2));



        addBehaviour(tbf.wrap(cyclicBehaviour1));
        addBehaviour(tbf2.wrap(cyclicBehaviour2));

    }


    CyclicBehaviour cyclicBehaviour1=new CyclicBehaviour() {
        @Override
        public void action() {
            Delay.msSleep(5000);
            logger.info("AAA");
            log.info("AAA");
            tbf.getThread(cyclicBehaviour1).interrupt();


        }
    };

    CyclicBehaviour cyclicBehaviour2 =new CyclicBehaviour() {
        @Override
        public void action() {
            Delay.msSleep(1000);
//            logger.info("BBB");
            log.info("BBB");

        }
    };

//    CyclicBehaviour cyclicBehaviour3=new CyclicBehaviour() {
//        @Override
//        public void action() {
//            System.out.println("CCC");
//            doWait(10000);
//        }
//    };



}
