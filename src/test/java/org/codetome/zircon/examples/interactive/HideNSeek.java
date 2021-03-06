package org.codetome.zircon.examples.interactive;

import org.codetome.zircon.api.Position;
import org.codetome.zircon.api.Size;
import org.codetome.zircon.api.Symbols;
import org.codetome.zircon.api.beta.component.GameComponent;
import org.codetome.zircon.api.beta.component.TextImageGameArea;
import org.codetome.zircon.api.builder.ComponentStylesBuilder;
import org.codetome.zircon.api.builder.TerminalBuilder;
import org.codetome.zircon.api.builder.TextCharacterBuilder;
import org.codetome.zircon.api.builder.TextImageBuilder;
import org.codetome.zircon.api.color.ANSITextColor;
import org.codetome.zircon.api.color.TextColor;
import org.codetome.zircon.api.color.TextColorFactory;
import org.codetome.zircon.api.component.Button;
import org.codetome.zircon.api.component.Panel;
import org.codetome.zircon.api.component.builder.ButtonBuilder;
import org.codetome.zircon.api.component.builder.PanelBuilder;
import org.codetome.zircon.api.font.Font;
import org.codetome.zircon.api.graphics.TextImage;
import org.codetome.zircon.api.input.InputType;
import org.codetome.zircon.api.resource.CP437TilesetResource;
import org.codetome.zircon.api.resource.ColorThemeResource;
import org.codetome.zircon.api.screen.Screen;
import org.codetome.zircon.api.terminal.Terminal;
import org.codetome.zircon.internal.graphics.BoxType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HideNSeek {

    private static final List<InputType> EXIT_CONDITIONS = new ArrayList<>();
    private static final int TERMINAL_WIDTH = 60;
    private static final int TERMINAL_HEIGHT = 30;
    private static final Size SIZE = Size.of(TERMINAL_WIDTH, TERMINAL_HEIGHT);
    private static final Font FONT = CP437TilesetResource.ROGUE_YUN_16X16.toFont();
    private static boolean headless = false;

    static {
        EXIT_CONDITIONS.add(InputType.Escape);
        EXIT_CONDITIONS.add(InputType.EOF);
    }

    @Test
    public void checkSetup() {
        main(new String[]{"test"});
    }

    public static void main(String[] args) {
        // for this example we only need a default terminal (no extra config)
        final Terminal terminal = TerminalBuilder.newBuilder()
                .font(FONT)
                .initialTerminalSize(SIZE)
                .buildTerminal(args.length > 0);
        if (args.length > 0) {
            headless = true;
        }
        final Screen screen = TerminalBuilder.createScreenFor(terminal);
        Size size = screen.getBoundableSize();
        screen.setCursorVisibility(false); // we don't want the cursor right now

        Panel actions = PanelBuilder.newBuilder()
                .size(screen.getBoundableSize().withColumns(20))
                .wrapWithBox()
                .title("Actions")
                .boxType(BoxType.TOP_BOTTOM_DOUBLE)
                .build();
        Button wait = ButtonBuilder.newBuilder()
                .text("Wait")
                .build();
        Button sleep = ButtonBuilder.newBuilder()
                .text("Sleep")
                .position(Position.DEFAULT_POSITION.withRelativeRow(1))
                .build();
        actions.addComponent(wait);
        actions.addComponent(sleep);
        screen.addComponent(actions);


        final Panel gamePanel = PanelBuilder.newBuilder()
                .size(screen.getBoundableSize().withColumns(40))
                .position(Position.DEFAULT_POSITION.relativeToRightOf(actions))
                .title("Game area")
                .wrapWithBox()
                .boxType(BoxType.TOP_BOTTOM_DOUBLE)
                .build();

        final Size visibleGameAreaSize = gamePanel.getBoundableSize().minus(Size.of(2, 2));
        final Size virtualGameAreaSize = Size.of(90, 90);

        final TextColor bgcolor = TextColorFactory.fromString("#332211");
        final TextImage gameField = TextImageBuilder.newBuilder()
                .size(virtualGameAreaSize)
                .filler(TextCharacterBuilder.newBuilder()
                        .character(Symbols.BLOCK_SPARSE)
                        .backgroundColor(bgcolor)
                        .foregroundColor(TextColorFactory.fromString("#665544"))
                        .build())
                .build();

        final Random random = new Random();
        final TextColor[] colors = new TextColor[]{
                TextColorFactory.fromString("#884433"),
                TextColorFactory.fromString("#773322"),
                TextColorFactory.fromString("#662211"),
                TextColorFactory.fromString("#553344")
        };
        final Character[] blocks = new Character[] {
                Symbols.BLOCK_DENSE,
                Symbols.BLOCK_MIDDLE,
                Symbols.BLOCK_SOLID,
        };
        for(int i = 0; i < 40; i++) {
            Position pos = Position.of(random.nextInt(90), random.nextInt(90));
            gameField.setCharacterAt(pos, TextCharacterBuilder.newBuilder()
                    .character(blocks[random.nextInt(3)])
                    .backgroundColor(bgcolor)
                    .foregroundColor(colors[random.nextInt(4)])
                    .build());
        }

        final GameComponent gameComponent = new GameComponent(
                new TextImageGameArea(gameField),
                visibleGameAreaSize,
                CP437TilesetResource.PHOEBUS_16X16.toFont(),
                Position.DEFAULT_POSITION,
                ComponentStylesBuilder.DEFAULT);
        screen.addComponent(gamePanel);
        gamePanel.addComponent(gameComponent);

        enableMovement(screen, gameComponent);
        screen.applyColorTheme(ColorThemeResource.SOLARIZED_DARK_CYAN.getTheme());
        screen.display();
    }

    private static void enableMovement(final Screen screen, final GameComponent gameComponent) {
        screen.onInput((input) -> {
            if (EXIT_CONDITIONS.contains(input.getInputType()) && !headless) {
                System.exit(0);
            } else {
                if (InputType.ArrowUp == input.getInputType()) {
                    gameComponent.scrollOneUp();
                }
                if (InputType.ArrowDown == input.getInputType()) {
                    gameComponent.scrollOneDown();
                }
                if (InputType.ArrowLeft == input.getInputType()) {
                    gameComponent.scrollOneLeft();
                }
                if (InputType.ArrowRight == input.getInputType()) {
                    gameComponent.scrollOneRight();
                }
                screen.refresh();
            }
        });
    }
}
