package com.acceleratetechnology.main;

import com.acceleratetechnology.controller.AbstractCommand;
import com.acceleratetechnology.controller.Command;
import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import org.apache.log4j.Logger;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;

/**
 * Main class.
 */
public class RAMainApplication {
    /**
     * Logs writer to console and to file.
     */
    private static Logger logger;

    /**
     * Find class that response for input command.
     *
     * @param command Input command
     * @return Command constructor.
     * @throws NoSuchMethodException Thrown when a particular method cannot be found.
     */
    private static Constructor init(String command) throws NoSuchMethodException {
        Reflections.log = null;//todo comment
        Reflections reflections = new Reflections(AbstractCommand.class.getPackage().getName());

        Set<Class<? extends AbstractCommand>> allClasses = reflections.getSubTypesOf(AbstractCommand.class);
        for (Class<?> allClass : allClasses) {
            Set<Constructor> injectables = Collections.singleton(allClass.getDeclaredConstructor(String[].class));

            for (Constructor m : injectables) {
                if (m.isAnnotationPresent(Command.class)) {
                    Command cmd = (Command) m.getAnnotation(Command.class);
                    if (cmd.value().equals(command)) {
                        return m;
                    }
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(RAMainApplication::showAlert);
        if (args.length > 0) {
            try {
                Constructor constructor = init(args[0]);
                AbstractCommand command = (AbstractCommand) (constructor.newInstance(new Object[]{args}));
                logger = Logger.getLogger(RAMainApplication.class);
                command.execute();

            } catch (NullPointerException e) {
                logger.error("Sorry but input command \"" + args[0] + "\" is not supported. Type -help to see commands.", e);
                System.exit(1);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                System.exit(1);
            }
        } else {
            System.setProperty("log.file", "logs.log");
            logger = Logger.getLogger(RAMainApplication.class);
            logger.info("Please enter a command. For more detail write -help");
            System.exit(1);
        }
    }

    /**
     * Send error message if unhandled exception or error was thrown.
     *
     * @param thread    Current thread.
     * @param throwable Throwable.
     */
    private static void showAlert(Thread thread, Throwable throwable) {
        logger.error("Something went wrong please try this application later.", throwable);
        System.exit(1);
    }
}
