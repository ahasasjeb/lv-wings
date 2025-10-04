package com.toni.wings.server.net;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class NetBuilder {
    private final ChannelBuilder builder;
    private Integer version;
    private SimpleChannel channel;
    private int id;

    public NetBuilder(ResourceLocation name) {
        this.builder = ChannelBuilder.named(name);
    }

    public NetBuilder version(int version) {
        if (this.version == null) {
            this.version = version;
            this.builder.networkProtocolVersion(version);
            return this;
        }
        throw new IllegalArgumentException("version already assigned");
    }

    public NetBuilder version(String version) {
        return this.version(Integer.parseInt(version));
    }

    public NetBuilder optionalServer() {
        this.ensureVersion();
        this.builder.optionalServer();
        return this;
    }

    public NetBuilder requiredServer() {
        this.ensureVersion();
        this.builder.clientAcceptedVersions(Channel.VersionTest.exact(this.version));
        return this;
    }

    public NetBuilder optionalClient() {
        this.ensureVersion();
        this.builder.optionalClient();
        return this;
    }

    public NetBuilder requiredClient() {
        this.ensureVersion();
        this.builder.serverAcceptedVersions(Channel.VersionTest.exact(this.version));
        return this;
    }
    
    private void ensureVersion() {
        if (this.version == null) {
            throw new IllegalStateException("version not specified");
        }
    }

    private SimpleChannel channel() {
        if (this.channel == null) {
            this.channel = this.builder.simpleChannel();
        }
        return this.channel;
    }

    public <T extends Message> MessageBuilder<T, ServerMessageContext> serverbound(Supplier<T> factory) {
        return new MessageBuilder<>(factory, new HandlerConsumerFactory<>(LogicalSide.SERVER, ServerMessageContext::new));
    }

    @SuppressWarnings("Convert2MethodRef")
    public <T extends Message> MessageBuilder<T, ClientMessageContext> clientbound(Supplier<T> factory) {
        return new MessageBuilder<>(factory, DistExecutor.unsafeRunForDist(() -> () -> createClientConsumerFactory(), () -> () -> createServerConsumerFactory()));
    }

    public SimpleChannel build() {
        return this.channel();
    }

    private static <T extends Message> ConsumerFactory<T, ClientMessageContext> createClientConsumerFactory() {
        return new HandlerConsumerFactory<>(LogicalSide.CLIENT, ClientMessageContext::new);
    }

    private static <T extends Message> ConsumerFactory<T, ClientMessageContext> createServerConsumerFactory() {
        return new NoopConsumerFactory<>();
    }

    interface ConsumerFactory<T extends Message, S extends MessageContext> {
        BiConsumer<T, CustomPayloadEvent.Context> create(Supplier<BiConsumer<? super T, S>> handlerFactory);
    }

    private static class NoopConsumerFactory<T extends Message, S extends MessageContext> implements ConsumerFactory<T, S> {
        @Override
        public BiConsumer<T, CustomPayloadEvent.Context> create(Supplier<BiConsumer<? super T, S>> handlerFactory) {
            return (msg, ctx) -> ctx.setPacketHandled(false);
        }
    }

    private static class HandlerConsumerFactory<T extends Message, S extends MessageContext> implements ConsumerFactory<T, S> {
        private final LogicalSide side;
        private final Function<CustomPayloadEvent.Context, S> contextFactory;

        HandlerConsumerFactory(LogicalSide side, Function<CustomPayloadEvent.Context, S> contextFactory) {
            this.side = side;
            this.contextFactory = contextFactory;
        }

        @Override
        public BiConsumer<T, CustomPayloadEvent.Context> create(Supplier<BiConsumer<? super T, S>> handlerFactory) {
            BiConsumer<? super T, S> handler = handlerFactory.get();
            return (msg, ctx) -> {
                boolean matchesSide = (this.side == LogicalSide.SERVER && ctx.isServerSide())
                    || (this.side == LogicalSide.CLIENT && ctx.isClientSide());
                if (matchesSide) {
                    S s = this.contextFactory.apply(ctx);
                    ctx.enqueueWork(() -> handler.accept(msg, s));
                }
                ctx.setPacketHandled(true);
            };
        }
    }

    public class MessageBuilder<T extends Message, S extends MessageContext> {
        private final Supplier<T> factory;
        private final ConsumerFactory<T, S> consumerFactory;

        protected MessageBuilder(Supplier<T> factory, ConsumerFactory<T, S> consumerFactory) {
            this.factory = factory;
            this.consumerFactory = consumerFactory;
        }

        public NetBuilder consumer(Supplier<BiConsumer<? super T, S>> consumer) {
            Supplier<T> factory = this.factory;
            @SuppressWarnings("unchecked") Class<T> type = (Class<T>) factory.get().getClass();
            NetBuilder.this.channel().messageBuilder(type, NetBuilder.this.id++)
                .encoder(Message::encode)
                .decoder(buf -> {
                    T msg = factory.get();
                    msg.decode(buf);
                    return msg;
                })
                .consumerMainThread(this.consumerFactory.create(consumer))
                .add();
            return NetBuilder.this;
        }
    }
}
