package taskmanagement.config;

import org.testcontainers.containers.MySQLContainer;

public class CustomMySQlContainer extends MySQLContainer<CustomMySQlContainer> {
    private static final String IMAGE = "mysql:8";

    private static CustomMySQlContainer mySQlContainer;

    private CustomMySQlContainer() {
        super(IMAGE);
    }

    public static synchronized CustomMySQlContainer getInstance() {
        if (mySQlContainer == null) {
            mySQlContainer = new CustomMySQlContainer();
        }
        return mySQlContainer;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("Test_DB_URL", mySQlContainer.getJdbcUrl());
        System.setProperty("Test_DB_USERNAME", mySQlContainer.getUsername());
        System.setProperty("Test_DB_PASSWORD", mySQlContainer.getPassword());
    }

    @Override
    public void stop() {
    }
}
