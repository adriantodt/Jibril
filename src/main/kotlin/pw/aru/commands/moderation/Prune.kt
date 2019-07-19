//package pw.aru.commands.moderation
//
//import net.dv8tion.jda.bot.Permission.MESSAGE_HISTORY
//import net.dv8tion.jda.bot.Permission.MESSAGE_MANAGE
//import pw.aru.bot.categories.Category
//import pw.aru.bot.commands.ICommand
//import pw.aru.bot.commands.context.CommandContext
//import pw.aru.bot.parser.Args
//import pw.aru.permissions.MemberPermissions.MESSAGES
//import pw.aru.permissions.Permissions
//
//class Prune : ICommand, ICommand.Permission {
//    override val category = Category.MODERATION
//    override val permissions = Permissions.AllOf(MESSAGES)
//
//    override fun CommandContext.call() {
//        val args = parseable()
//
//        if (!requirePerms(MESSAGE_MANAGE, MESSAGE_HISTORY)) return
//
//        val arg = args.takeString()
//        when (arg) {
//            "" -> showHelp()
//            "member" -> pruneMembers(args)
//            "bot" -> pruneBot(args)
//            else -> {
//                val i = arg.toIntOrNull() ?: return showHelp()
//                pruneAmount(i, args)
//            }
//        }
//    }
//
//    private fun CommandContext.pruneAmount(amount: Int, args: Args) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    private fun CommandContext.pruneBot(args: Args) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    private fun CommandContext.pruneMembers(args: Args) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//}