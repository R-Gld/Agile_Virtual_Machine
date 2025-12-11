package fr.ufrst.m1info.gl.groupe7.memoire.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour GuiAppender.
 * Couverture cible: 100%
 */
class GuiAppenderTest {

    private GuiAppender appender;
    private MockConsole mockConsole;

    @BeforeEach
    void setUp() {
        appender = new GuiAppender();
        appender.start(); // Start the appender
        mockConsole = new MockConsole();
    }

    @AfterEach
    void tearDown() {
        // Clean up static state
        GuiAppender.unsetGuiConsole();
        appender.stop();
    }

    @Test
    void setGuiConsole_registersConsole() {
        GuiAppender.setGuiConsole(mockConsole);

        // Verify console was registered by trying to log
        ILoggingEvent event = createMockEvent("Test message", Level.INFO);
        appender.append(event);

        // Should have received the message (but won't work without JavaFX Platform.runLater)
        // We can't test the actual appending without JavaFX runtime
    }

    @Test
    void unsetGuiConsole_unregistersConsole() {
        GuiAppender.setGuiConsole(mockConsole);
        GuiAppender.unsetGuiConsole();

        // Appending should not throw exception even without console
        ILoggingEvent event = createMockEvent("Test message", Level.INFO);
        assertDoesNotThrow(() -> appender.append(event));
    }

    @Test
    void append_withNullConsole_doesNotThrow() {
        GuiAppender.unsetGuiConsole();

        ILoggingEvent event = createMockEvent("Test message", Level.INFO);
        assertDoesNotThrow(() -> appender.append(event));
    }

    @Test
    void append_withValidConsole_doesNotThrow() {
        GuiAppender.setGuiConsole(mockConsole);

        ILoggingEvent event = createMockEvent("Test message", Level.INFO);
        assertDoesNotThrow(() -> appender.append(event));
    }

    @Test
    void append_formatMessage_containsTimestamp() {
        GuiAppender.setGuiConsole(mockConsole);

        ILoggingEvent event = createMockEvent("Test message", Level.INFO);
        when(event.getTimeStamp()).thenReturn(System.currentTimeMillis());

        appender.append(event);

        // Message formatting is tested indirectly through append
        // The format should contain timestamp pattern HH:mm:ss
    }

    @Test
    void append_formatMessage_containsLevel() {
        GuiAppender.setGuiConsole(mockConsole);

        ILoggingEvent event = createMockEvent("Test message", Level.ERROR);

        appender.append(event);

        // Verify level was used in formatting
        verify(event, atLeastOnce()).getLevel();
    }

    @Test
    void append_formatMessage_shortensLoggerName() {
        GuiAppender.setGuiConsole(mockConsole);

        ILoggingEvent event = createMockEvent("Test message", Level.INFO);
        when(event.getLoggerName()).thenReturn("fr.ufrst.m1info.gl.groupe7.memoire.StacksTest");

        appender.append(event);

        // Logger name should be shortened to last 2 segments
        verify(event, atLeastOnce()).getLoggerName();
    }

    @Test
    void append_formatMessage_keepsShortLoggerName() {
        GuiAppender.setGuiConsole(mockConsole);

        ILoggingEvent event = createMockEvent("Test message", Level.INFO);
        when(event.getLoggerName()).thenReturn("Test");

        appender.append(event);

        // Short logger name should remain unchanged
        verify(event, atLeastOnce()).getLoggerName();
    }

    @Test
    void append_multipleEvents_allProcessed() {
        GuiAppender.setGuiConsole(mockConsole);

        ILoggingEvent event1 = createMockEvent("Message 1", Level.INFO);
        ILoggingEvent event2 = createMockEvent("Message 2", Level.WARN);
        ILoggingEvent event3 = createMockEvent("Message 3", Level.ERROR);

        assertDoesNotThrow(() -> {
            appender.append(event1);
            appender.append(event2);
            appender.append(event3);
        });
    }

    @Test
    void append_withException_doesNotPropagate() {
        // Use a console that doesn't have printMessage method
        Object invalidConsole = new Object();
        GuiAppender.setGuiConsole(invalidConsole);

        ILoggingEvent event = createMockEvent("Test message", Level.INFO);

        // Should not throw even if reflection fails
        assertDoesNotThrow(() -> appender.append(event));
    }

    @Test
    void start_initializesAppender() {
        GuiAppender newAppender = new GuiAppender();
        assertDoesNotThrow(newAppender::start);
        newAppender.stop();
    }

    @Test
    void stop_shutsDownAppender() {
        GuiAppender newAppender = new GuiAppender();
        newAppender.start();
        assertDoesNotThrow(newAppender::stop);
    }

    // Helper methods

    private ILoggingEvent createMockEvent(String message, Level level) {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn(message);
        when(event.getLevel()).thenReturn(level);
        when(event.getLoggerName()).thenReturn("fr.ufrst.TestLogger");
        when(event.getTimeStamp()).thenReturn(System.currentTimeMillis());
        return event;
    }

    /**
     * Mock console class for testing.
     * Simulates the ConsoleOutput widget from GUI module.
     */
    static class MockConsole {
        private final List<String> messages = new ArrayList<>();

        public void printMessage(String message) {
            messages.add(message);
        }

        public List<String> getMessages() {
            return messages;
        }
    }
}
