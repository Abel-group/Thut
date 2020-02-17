package thut.core.common.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

public class CommandTools
{
    public static boolean hasPerm(final CommandSource source, final String permission)
    {
        try
        {
            final ServerPlayerEntity player = source.asPlayer();
            return CommandTools.hasPerm(player, permission);
        }
        catch (final CommandSyntaxException e)
        {
            // TODO decide what to actually do here?
            return true;
        }
    }

    public static boolean hasPerm(final ServerPlayerEntity player, final String permission)
    { /*
       * Check if the node is registered, if not, register it as OP, and send
       * error message about this.
       */
        if (!PermissionAPI.getPermissionHandler().getRegisteredNodes().contains(permission))
        {
            final String message = "Autogenerated node, this is a bug and should be pre-registered.";
            PermissionAPI.getPermissionHandler().registerNode(permission, DefaultPermissionLevel.OP, message);
            System.err.println(message + ": " + permission);
        }
        return PermissionAPI.hasPermission(player, permission);
    }

    public static ITextComponent makeError(final String text)
    {
        return CommandTools.makeTranslatedMessage(text, "red:italic");
    }

    public static ITextComponent makeTranslatedMessage(final String key, String formatting, final Object... args)
    {
        if (formatting == null) formatting = "";
        for (int i = 0; i < args.length; i++)
            if (args[i] instanceof String) args[i] = new TranslationTextComponent((String) args[i]);
        final TranslationTextComponent translated = new TranslationTextComponent(key, args);
        if (!formatting.isEmpty())
        {
            final String[] args2 = formatting.split(":");
            final String colour = args2[0].toUpperCase(java.util.Locale.ROOT);
            translated.getStyle().setColor(TextFormatting.getValueByName(colour));
            if (args2.length > 1) for (int i1 = 1; i1 < args2.length; i1++)
            {
                final String arg = args2[i1];
                if (arg.equalsIgnoreCase("italic")) translated.getStyle().setItalic(true);
                if (arg.equalsIgnoreCase("bold")) translated.getStyle().setBold(true);
                if (arg.equalsIgnoreCase("underlined")) translated.getStyle().setUnderlined(true);
                if (arg.equalsIgnoreCase("strikethrough")) translated.getStyle().setStrikethrough(true);
                if (arg.equalsIgnoreCase("obfuscated")) translated.getStyle().setObfuscated(true);
            }
        }
        return translated;
    }

    public static void sendBadArgumentsMissingArg(final ICommandSource sender)
    {
        sender.sendMessage(CommandTools.makeError("pokecube.command.invalidmissing"));
    }

    public static void sendBadArgumentsTryTab(final ICommandSource sender)
    {
        sender.sendMessage(CommandTools.makeError("pokecube.command.invalidtab"));
    }

    public static void sendError(final CommandSource sender, final String text)
    {
        sender.sendErrorMessage(CommandTools.makeError(text));
    }

    public static void sendError(final ICommandSource sender, final String text)
    {
        sender.sendMessage(CommandTools.makeError(text));
    }

    public static void sendMessage(final ICommandSource sender, final String text)
    {
        final ITextComponent message = CommandTools.makeTranslatedMessage(text, null);
        sender.sendMessage(message);
    }

    public static void sendNoPermissions(final ICommandSource sender)
    {
        sender.sendMessage(CommandTools.makeError("pokecube.command.noperms"));
    }
}
