package net.thenoppy12.ngroklan.mixin;


import net.thenoppy12.ngroklan.NgrokLan;
import net.thenoppy12.ngroklan.config.NLanConfig;
import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.conf.JavaNgrokConfig;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Proto;
import com.github.alexdlaird.ngrok.protocol.Region;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OpenToLanScreen.class)
public class OpenToLanScreenMixin extends Screen {
    NLanConfig config = AutoConfig.getConfigHolder(NLanConfig.class).getConfig();
    MinecraftClient mc = MinecraftClient.getInstance();

    @Unique
    public GameMode gameMode;
    @Unique
    private final boolean allowCommands = false;

    protected OpenToLanScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void initWidgets(CallbackInfo info) {

        if (config.enabledCheckBox) {
            this.addButton(new ButtonWidget(this.width / 2 - 155, this.height - 58, 150, 20, new TranslatableText("text.UI.ngroklan.LanButton"), (button) -> {
                int localPort = NetworkUtils.findLocalPort();
                this.client.openScreen(null);
                switch (config.regionSelect) {
                    case EU -> ngrokInit(localPort, Region.EU);
                    case AP -> ngrokInit(localPort, Region.AP);
                    case AU -> ngrokInit(localPort, Region.AU);
                    case SA -> ngrokInit(localPort, Region.SA);
                    case JP -> ngrokInit(localPort, Region.JP);
                    case IN -> ngrokInit(localPort, Region.IN);
                    default -> ngrokInit(localPort, Region.US); //US bundled here
                }
            }));

        }
    }

    public void ngrokInit(int port, Region region) {

        Thread thread = new Thread(() -> {

            if (config.authToken.equals("AuthToken")) {
                mc.inGameHud.getChatHud().addMessage(new TranslatableText("text.error.ngroklan.AuthTokenError").formatted(Formatting.RED));
            } else {
                try {
                    NgrokLan.LOGGER.info("Launched Lan!");

                    mc.inGameHud.getChatHud().addMessage(new TranslatableText("text.info.ngroklan.startMessage").formatted(Formatting.YELLOW));

                    final JavaNgrokConfig javaNgrokConfig = new JavaNgrokConfig.Builder()
                            .withAuthToken(config.authToken)
                            .withRegion(region)
                            .build();

                    NgrokLan.ngrokClient = new NgrokClient.Builder()
                            .withJavaNgrokConfig(javaNgrokConfig)
                            .build();

                    final CreateTunnel createTunnel = new CreateTunnel.Builder()
                            .withProto(Proto.TCP)
                            .withAddr(port)
                            .build();

                    final Tunnel tunnel = NgrokLan.ngrokClient.connect(createTunnel);

                    NgrokLan.LOGGER.info(tunnel.getPublicUrl());

                    var ngrok_url = tunnel.getPublicUrl().substring(6);

                    mc.inGameHud.getChatHud().addMessage(new TranslatableText("text.info.ngroklan.success").formatted(Formatting.GREEN));
                    mc.inGameHud.getChatHud().addMessage( new TranslatableText("text.info.ngroklan.ip", ("\u00a7e" + ngrok_url + "\u00a7f")));
                    mc.keyboard.setClipboard(ngrok_url);

                    TranslatableText text;


                    if (this.client.getServer().openToLan(this.gameMode, this.allowCommands ,port)) {
                        mc.getServer().setOnlineMode(config.onlineCheckBox);
                        text = new TranslatableText("commands.publish.started", port);
                    } else {
                        text = new TranslatableText("commands.publish.failed");
                    }
                    this.client.inGameHud.getChatHud().addMessage(text);
                    this.client.updateWindowTitle();

                } catch (Exception error) {
                    error.printStackTrace();
                    mc.inGameHud.getChatHud().addMessage(new LiteralText(error.getMessage()));
                    mc.inGameHud.getChatHud().addMessage(new TranslatableText("text.error.ngroklan.fail").formatted(Formatting.RED));
                    throw new RuntimeException("Ngrok Service Failed to Start" + error.getMessage());
                }
            }
        });

        thread.start();

    }

}
