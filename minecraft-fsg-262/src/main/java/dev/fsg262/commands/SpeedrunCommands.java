package dev.fsg262.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.fsg262.config.FsgConfig;
import dev.fsg262.evaluation.EvaluatorRuntime;
import dev.fsg262.filter.FilterProfile;
import dev.fsg262.filter.SeedFilter;
import dev.fsg262.rng.LegacyPiglinBartering;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class SpeedrunCommands {
    private SpeedrunCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mcsr")
                .then(Commands.literal("debug")
                        .then(Commands.literal("analyze")
                                .then(Commands.argument("seed", LongArgumentType.longArg())
                                        .executes(SpeedrunCommands::analyze)))));
        dispatcher.register(Commands.literal("speedrun")
                .then(Commands.literal("version").executes(SpeedrunCommands::version))
                .then(Commands.literal("config").executes(SpeedrunCommands::config))
                .then(Commands.literal("rng").executes(SpeedrunCommands::rng))
                .then(Commands.literal("barter").executes(SpeedrunCommands::barter))
                .then(Commands.literal("seedinfo")
                        .then(Commands.argument("seed", LongArgumentType.longArg())
                                .executes(SpeedrunCommands::seedInfo)))
                .then(Commands.literal("filter")
                        .then(Commands.argument("seed", LongArgumentType.longArg())
                                .executes(SpeedrunCommands::filter)))
                .then(Commands.literal("filterrange")
                        .then(Commands.argument("start", LongArgumentType.longArg())
                                .then(Commands.argument("end", LongArgumentType.longArg())
                                        .executes(SpeedrunCommands::filterRange)))));
    }

    private static int version(CommandContext<CommandSourceStack> context) {
        return reply(context, "FSG Seed Types 26.2 · filter fsg262-filter-0.1.0");
    }

    private static int config(CommandContext<CommandSourceStack> context) {
        var config = FsgConfig.defaults();
        return reply(context, "Profile: " + config.profileName()
                + " · standardized RNG: " + config.standardizedRng()
                + " · legacy piglin: " + config.legacyPiglinBehavior());
    }

    private static int rng(CommandContext<CommandSourceStack> context) {
        return reply(context, "RNG seed defaults to the Overworld seed; separate RNG seeds are supported.");
    }

    private static int barter(CommandContext<CommandSourceStack> context) {
        return reply(context, new LegacyPiglinBartering().replay(0L, 8).trim());
    }

    private static int seedInfo(CommandContext<CommandSourceStack> context) {
        long seed = LongArgumentType.getLong(context, "seed");
        return reply(context, "Seed " + seed + " · structure adapter status: TODO — NEEDS VERIFICATION");
    }

    private static int analyze(CommandContext<CommandSourceStack> context) {
        long seed = LongArgumentType.getLong(context, "seed");
        var result = EvaluatorRuntime.orchestrator()
                .evaluate(seed, FilterProfile.strictRankedStyle());
        return reply(context, result.report());
    }

    private static int filter(CommandContext<CommandSourceStack> context) {
        long seed = LongArgumentType.getLong(context, "seed");
        var profile = FilterProfile.strictRankedStyle();
        var filter = new SeedFilter(new dev.fsg262.filter.UnverifiedWorldGenerationAnalyzer());
        return reply(context, filter.report(seed, profile).trim());
    }

    private static int filterRange(CommandContext<CommandSourceStack> context) {
        long start = LongArgumentType.getLong(context, "start");
        long end = LongArgumentType.getLong(context, "end");
        if (end < start) return reply(context, "End seed must be >= start seed.");
        long count = end - start + 1;
        if (count > 100_000) return reply(context, "Refusing to scan more than 100,000 seeds in one command.");
        return reply(context, "Queued explicit filter range " + start + ".." + end
                + " (" + count + " seeds). World-generation adapter status: TODO — NEEDS VERIFICATION");
    }

    private static int reply(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> Component.literal(message), false);
        return 1;
    }
}