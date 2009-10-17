package net;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public enum SendPacketOpcode implements WritableIntValueHolder {
    LOGIN_STATUS,// 0x00
    SEND_LINK,// 0x01
    SERVERSTATUS,// 0x03
    GENDER_DONE,// 0x04
    PIN_OPERATION,// 0x06
    PIN_ASSIGNED,// 0x07
    ALL_CHARLIST,// 0x08
    PING,// 0x11
    SERVERLIST,// 0x0A
    CHARLIST,// 0x0B
    SERVER_IP,// 0x0C
    CHAR_NAME_RESPONSE,// 0x0D
    ADD_NEW_CHAR_ENTRY,// 0x0E
    DELETE_CHAR_RESPONSE,// 0x0F
    CHANGE_CHANNEL,// 0x10
    CHANNEL_SELECTED,// 0x14
    RELOG_RESPONSE,// 0x16
    MODIFY_INVENTORY_ITEM,// 0x1A
    UPDATE_STATS,// 0x1C
    GIVE_BUFF,// 0x1D
    CANCEL_BUFF,// 0x1E
    UPDATE_SKILLS,// 0x21
    FAME_RESPONSE,// 0x23
    SHOW_STATUS_INFO,// 0x24
    SHOW_NOTES,// 0x26
    TROCK_LOCATIONS,// 0x27
    UPDATE_MOUNT,// 0x2D
    SHOW_QUEST_COMPLETION,// 0x2E
    USE_SKILL_BOOK,// 0x30
    REPORT_PLAYER_MSG,// 0x34
    REPORTREPLY,// 0x34
    GENDER,// 0x37
    BBS_OPERATION,// 0x38
    CHAR_INFO,// 0x3A
    PARTY_OPERATION,// 0x3B
    BUDDYLIST,// 0x3C
    DUEY_ACTION,// 0x3D
    GUILD_OPERATION,// 0x3E
    ALLIANCE_OPERATION,// 0x3F
    SPAWN_PORTAL,// 0x40
    SERVERMESSAGE,// 0x41
    YELLOW_TIP,// 0x4A
    PLAYER_NPC,// 0x4E
    AVATAR_MEGA,// 0x54
    GM_POLICE,// 0x59
    SKILL_MACRO,// 0x5B
    WARP_TO_MAP,// 0x5C
    MTS_OPEN,// 0x5D
    CS_OPEN,// 0x5E
    BLOCK_PORTAL,// 0x62
    SHOW_EQUIP_EFFECT,// 0x63
    MULTICHAT,// 0x64
    WHISPER,// 0x65
    SPOUSECHAT,// 0x66
    BOSS_ENV,// 0x68
    MAP_EFFECT,// 0x69
    OX_QUIZ,// 0x6C
    GMEVENT_INSTRUCTIONS,// 0x6D
    CLOCK,// 0x6E
    BOAT_EFFECT,// 0x6F
    ARIANT_SCOREBOARD,// 0x76
    SPAWN_PLAYER,// 0x78
    REMOVE_PLAYER_FROM_MAP,// 0x79
    CHATTEXT,// 0x7A
    CHALKBOARD,// 0x7B
    UPDATE_CHAR_BOX,// 0x7c
    SHOW_SCROLL_EFFECT,// 0x7E
    SPAWN_PET,// 0x7F
    MOVE_PET,// 0x81
    PET_CHAT,// MOVE_PET 1
    PET_NAMECHANGE,// MOVE_PET 2
    PET_COMMAND,// MOVE_PET 4
    SPAWN_SPECIAL_MAPOBJECT,// 0x86
    REMOVE_SPECIAL_MAPOBJECT,// 0x87
    MOVE_SUMMON,// 0x88
    SUMMON_ATTACK,// 0x89
    DAMAGE_SUMMON,// 0x8A
    SUMMON_SKILL,// 0x8B
    MOVE_PLAYER,// 0x8D
    CLOSE_RANGE_ATTACK,// 0x8E
    RANGED_ATTACK,// 0x8F
    MAGIC_ATTACK,// 0x90
    SKILL_EFFECT,// 0x92
    CANCEL_SKILL_EFFECT,// 0x93
    DAMAGE_PLAYER,// 0x94
    FACIAL_EXPRESSION,// 0x95
    SHOW_ITEM_EFFECT,// FACIAL_EXPRESSION 1
    SHOW_CHAIR,// 0x97
    UPDATE_CHAR_LOOK,// 0x98
    SHOW_FOREIGN_EFFECT,// 0x99
    GIVE_FOREIGN_BUFF,// 0x9A
    CANCEL_FOREIGN_BUFF,// 0x9B
    UPDATE_PARTYMEMBER_HP,// 0x9C
    CANCEL_CHAIR,// 0xA0
    SHOW_ITEM_GAIN_INCHAT,// 0xA1
    UPDATE_QUEST_INFO,// 0xA6
    PLAYER_HINT,// 0xA9
    COOLDOWN,// 0xAD
    SPAWN_MONSTER,// 0xAF
    KILL_MONSTER,// 0xB0
    SPAWN_MONSTER_CONTROL,// 0xB1
    MOVE_MONSTER,// 0xB2
    MOVE_MONSTER_RESPONSE,// 0xB3
    APPLY_MONSTER_STATUS,// 0xB5
    CANCEL_MONSTER_STATUS,// 0xB6
    DAMAGE_MONSTER,// 0xB9
    ARIANT_THING,// 0xBC
    SHOW_MONSTER_HP,// 0xBD
    SHOW_MAGNET,// 0xBE
    CATCH_MONSTER,// 0xBF
    SPAWN_NPC,// 0xC2
    NPC_CONFIRM,// 0xC3
    SPAWN_NPC_REQUEST_CONTROLLER,// 0xC4
    NPC_ACTION,// 0xC5
    SPAWN_HIRED_MERCHANT,// 0xCA
    DESTROY_HIRED_MERCHANT,// 0xCB
    DROP_ITEM_FROM_MAPOBJECT,// 0xCD
    REMOVE_ITEM_FROM_MAP,// 0xCE
    SPAWN_MIST,// 0xD2
    REMOVE_MIST,// 0xD3
    SPAWN_DOOR,// 0xD4
    REMOVE_DOOR,// 0xD5
    REACTOR_HIT,// 0xD6
    REACTOR_SPAWN,// 0xD8
    REACTOR_DESTROY,// 0xD9
    MONSTER_CARNIVAL_START,// 0xE2
    MONSTER_CARNIVAL_OBTAINED_CP,// 0xE3
    MONSTER_CARNIVAL_PARTY_CP,// 0xE4
    MONSTER_CARNIVAL_SUMMON,// 0xE5
    MONSTER_CARNIVAL_DIED,// 0xE7
    ARIANT_PQ_START,// 0xEA
    ZAKUM_SHRINE,// 0xEC
    NPC_TALK,// 0xED
    OPEN_NPC_SHOP,// 0xEE
    CONFIRM_SHOP_TRANSACTION,// 0xEF
    OPEN_STORAGE,// 0xF0
    MESSENGER,// 0xF4
    PLAYER_INTERACTION,// 0xF5
    DUEY,// 0xFD
    CS_UPDATE,// 0xFF
    CS_OPERATION,// 0x100
    KEYMAP,// 0x107
    AUTO_HP_POT,// 0x108
    AUTO_MP_POT,// 0x109
    TV_SMEGA,// 0x10D
    CANCEL_TV_SMEGA,// 0x10E
    MTS_OPERATION,// 0x114
    GET_MTS_TOKENS,// 0x113
    MTS_OPERATION2,// 0x113
    SEND_TV,// 0x10D
    REMOVE_TV,// 0x10E
    ENABLE_TV;// 0x10F
    private int code = -2;

    public void setValue(int code) {
        this.code = code;
    }

    @Override
    public int getValue() {
        return code;
    }

    public static Properties getDefaultProperties() throws FileNotFoundException, IOException {
        Properties props = new Properties();
        FileInputStream fileInputStream = new FileInputStream(System.getProperty("sendops"));
        props.load(fileInputStream);
        fileInputStream.close();
        return props;
    }


    static {
        try {
            ExternalCodeTableGetter.populateValues(getDefaultProperties(), values());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load sendops", e);
        }
    }
}