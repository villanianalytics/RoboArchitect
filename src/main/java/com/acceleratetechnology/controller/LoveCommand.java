package com.acceleratetechnology.controller;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import org.apache.log4j.Logger;

import java.io.IOException;

public class LoveCommand extends AbstractCommand {
    private Logger logger = Logger.getLogger(getClass());
    @Command("-iLoveRoboArchitect")
    public LoveCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    @Override
    public void execute() {
    	logger.trace("LoveCommand.command started");
        logResponse("                                                                                                    \n" +
                "                                      ``..-:::////////::--..`                                       \n" +
                "                               `.:/+oosssssssssssssssssssssssso++/-.`                               \n" +
                "                         `-+:`/ossssssssssssssssssssssssssssssssssso+/`/+-                          \n" +
                "                      -ohNNMMs`:+++++++///ossssssssssssso+///+++++++:`sMMNNh+-                      \n" +
                "                  `:smNMMMMMMMd::::://++o/`+oooooooooooo/`/o++//::::/dMMMMMMMNms:`                  \n" +
                "                -smMMMMMNhyo//:::::::::/smy.------------.yms/::::::::://oydNMMMMMmo-                \n" +
                "              :hNMMMmy+::/oyhhdddddddddy/-osyyyyyyyyyyyys+-/hdddddddddhhso/::+hmMMMNy-              \n" +
                "            .yNMMNy/:+shdmmmNNNNNNNNNNNNmhs++++++++++++++shmNNNNNNNNNNNNmmmdhs/:/yNMMNy.            \n" +
                "           +NMMNs-/ydmmNNNNNNNNNNNNNNNNNNNmmmmmmmmmmmmmmmmNNNNNNNNNNNNNNNNNNNmmdy/-sNMMN+           \n" +
                "         `yMMMd-:ymmNNNNNNNNNNNNNmdddddmNNNNNNNNNNNNNNNNNNNNNmddddmNNNNNNNNNNNNNmmy:-dMMMs`         \n" +
                "     `.` yMMMy.omNNNNNNNNNNNNNNd+.......+dNNNNNNNNNNNNNNNNNy:......-sNNNNNNNNNNNNNNd+.hMMMs `.`     \n" +
                "   `:++`oMMMd`omNNNNNNNNNNNNNNN-         :NNNNNNNNNNNNNNNNh          yNNNNNNNNNNNNNNmo`dMMM+`++:`   \n" +
                "  `/++-.NMMM-:mNNNNNNNNNNNNNNNN`         `NNNNNNNNNNNNNNNNs          oNNNNNNNNNNNNNNNm-:MMMN`-++/   \n" +
                "  -+++`oMMMm yNNNNNNNNNNNNNNNNN`         `NNNNNNNNNNNNNNNNs          oNNNNNNNNNNNNNNNNs mMMMo`+++-  \n" +
                "  +++/ dMMMy mNNNNNNNNNNNNNNNNN`         `NNNNNNNNNNNNNNNNs          oNNNNNNNNNNNNNNNNd hMMMd /+++  \n" +
                " `+++: NMMMy NNNNNNNNNNNNNNNNNN`         `NNNNNNNNNNNNNNNNs          oNNNNNNNNNNNNNNNNm yMMMm /+++` \n" +
                " `+++: NMMMh dNNNNNNNNNNNNNNNNN`         `NNNNNNNNNNNNNNNNs          oNNNNNNNNNNNNNNNNd dMMMm /+++` \n" +
                " `+++/ hMMMN`sNNNNNNNNNNNNNNNNN`         `NNNNNNNNNNNNNNNNs          oNNNNNNNNNNNNNNNNo`NMMMh ++++` \n" +
                "  ++++`+MMMM/-mNNNNNNNNNNNNNNNN`         `NNNNNNNNNNNNNNNNs          oNNNNNNNNNNNNNNNm.+MMMM/`++++  \n" +
                "  :+++-`mMMMm`sNNNNNNNNNNNNNNNN`         `NNNNNNNNNNNNNNNNs          oNNNNNNNNNNNNNNNo`mMMMm`:+++:  \n" +
                "  .++++`+NMMMy`hNNNNNNNNNNNNNNN.         .NNNNNNNNNNNNNNNNs          sNNNNNNNNNNNNNNh`yMMMN/`++++`  \n" +
                "   :/++/ yMMMMo.hNNNNNNNNNNNNNNs`       `sNNNNNNNNNNNNNNNNm-        -dNNNNNNNNNNNNNh`sMMMMy /++/:   \n" +
                "   `////-`hMMMMs`yNNNNNNNNNNNNNNdso+++osdNNNNNNNNNNNNNNNNNNNho++++oymNNNNNNNNNNNNms`sMMMNh`-////`   \n" +
                "    .////-`yNMMMd-/dNNNNNNNNNNNNNNNNNNNNNNNN+sNNNNNNNNh/NNNNNNNNNNNNNNNNNNNNNNNNd/-dMMMNy`-////.    \n" +
                "     .////:`oNMMMMs.+dNNNNNNNNNNNNNNNNNNNNNNs`smNNNNms`oNNNNNNNNNNNNNNNNNNNNNNd+.sMMMMNo`:////.     \n" +
                "      `-:::. :dNMMMNs./yNNNNNNNNNNNNNNNNNNNNNd/-://:-/hNNNNNNNNNNNNNNNNNNNNNy:-sNMMMNd- -:::-`      \n" +
                "               /dNNMMMd+-/ymNNNNNNNNNNNNNNNNNNNNNmmNNNNNNNNNNNNNNNNNNNNNmy/.+dMMMNNd/               \n" +
                "                 :ymNNMMMho::+ymNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNmy+::odMMMNNmy:                 \n" +
                "                   `/sdNNNMMNho/::+shdmNNNNNNNNNNNNNNNNNNNNNNmdhs+::/ohNMMMNNmy/`                   \n" +
                "                      `-/shmNNNNNNdyo+::://++oooooooooo++//::/+oydNNNNNNNmyo:`                      \n" +
                "                       .-:--:/oyhdmNNNNNNmmddhhhhhhhhhhddmNNNNNNNNmdhyo/----.                       \n" +
                "                     :osssoo++/.`/:://+osyyhhhdddddddddhhhyyso+//::/`-/++oosso/.                    \n" +
                "                   `ossssssssso.:mmmddhysoo+++////////++++oosyhddmmN.-osssssssss/                   \n" +
                "                  `.-/osssssso+ yMMMMNNNNNNNNNNNNNNNNNNNNNNNMMMMMMMMo`+osssssss+:`                  \n" +
                "                  /+/:-.-/+ooo-.MMMMMMMMNmmmmmmmmmmmmmmmmmmmNMMMMMMMm`:osso+/-.-:/-                 \n" +
                "                  /+++++/:--.. yMMMMMMy//++++++++++++++++++++:oNMMMMMo`-----:/++++/                 \n" +
                "                 :-.://++++/:`+MMMMMMs.dNMMMMMMMMMMMMMMNmmdNMm/-MMMMMN-`:/++++++/-..                \n" +
                "                `mmho/:-----``NMMMMMM:/MMMMMMMMMhhhNMMmo+++NMMh NMMMMMm..:::----/sdh                \n" +
                "                `dMMNNmddhs` +MMMMMMM:/MMMMMMMMhoo+yNy++syhMMMh NMMMMMMy `+oshdmNNNm                \n" +
                "                 -dMMMMMNh:  hMMMMMMM:/MMMMMMNdssdsso+ymMMMMMMh NMMMMMMN  omMMMMMMm/                \n" +
                "                  -/osso:`  `MMMMMMMM:/MMMMMhsssyNy+ohMMMMMMMMh NMMMMMMM-  -ohddy+-                 \n" +
                "                  ydhyyh`   -MMMMMMMM:/MMMMMNsssos+yssmMMMMMMMh NMMMMMMM+   `ooooyo                 \n" +
                "                  hNmhss.   /MMMMMMMM::MMMMMmhhmyohMdhdMMMMMMMh NMMMMMMMs   -ddmNNy                 \n" +
                "                  o:-:/++:. /NMMMMMMMs.hNMMMMMMMNdMMMMMMMMMMNm:-MMMMMMMMo `-:::::oy                 \n" +
                "                  .+sssssss+.:mMMMMMMMh///++++++++++++++++++//sNMMMMMMN+./ossssso:`                 \n" +
                "                 `+ssssssssss:`dNMMMMMMMMNNNNNNNNNNNNNNNNNNNNMMMMMMMNm--osssssssss/                 \n" +
                "                 -osssssssssss`.:/mMMMMMMMMMNNmdhyyyhdmNNMMMMMMMMMNo/..ssssssssssso.                \n" +
                "                 -oossssssssss`:y`oMMMMMNho/----:::::----/oymMMMMM+.d:.ssssssssssoo.                \n" +
                "                  +oossssssss: `/yNNNms:.-/+++++++++++++++/-.:smNNm+-  +ssssssssoo+`                \n" +
                "                  .+ooooooo+-    -::-.-/+++++++++++++++++++++/-.-:::`  `:oooooooo+.                 \n" +
                "                  ..-://:-``yy      `.-:///++++++++++++++++///:-`      o+`-:++++:.`                 \n" +
                "                  -y`  +y`  ..           ``..---:::::---...`           :/  .+:  .h-                 \n" +
                "                        .                                                   /.                      \n");
    }
}



