package co.flock;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(9000);
        XmlWebApplicationContext context = new XmlWebApplicationContext();
        context.setConfigLocation("classpath:/context.xml");

        ServletHolder servletHolder = new ServletHolder(new DispatcherServlet(context));
        servletHolder.setInitOrder(1);
        ServletContextHandler servletContext = new ServletContextHandler(server, "/");
        servletContext.addServlet(servletHolder, "/*");

        server.setHandler(servletContext);
        server.start();
    }
}
