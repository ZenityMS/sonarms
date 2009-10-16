package net;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public enum RecvPacketOpcode implements WritableIntValueHolder {
    LOGIN_PASSWORD,// 0x01
    GUEST_LOGIN,// 0x02
    SERVERLIST_REREQUEST,// 0x04
    CHARLIST_REQUEST,// 0x05
    SERVERSTATUS_REQUEST,// 0x06
    SET_GENDER,// 0x08
    AFTER_LOGIN,// 0x09
    REGISTER_PIN,// 0x0A
    SERVERLIST_REQUEST,// 0x0B
    VIEW_ALL_CHAR,// 0x0D
    PICK_ALL_CHAR,// 0x0E
    CHAR_SELECT,// 0x13
    CHECK_CHAR_NAME,// CHAR_SELECT 2
    CREATE_CHAR,// CHAR_SELECT 3
    DELETE_CHAR,// CHAR_SELECT 4
    PONG,// 0x18
    CLIENT_START,// 0x19
    RELOG,// 0x1C
    PLAYER_LOGGEDIN,// 0x14
    STRANGE_DATA,// 0x1A
    CHANGE_MAP,// 0x23
    CHANGE_CHANNEL,// 0x24
    ENTER_CASH_SHOP,// 0x25
    MOVE_PLAYER,// 0x26
    CANCEL_CHAIR,// 0x27
    USE_CHAIR,// 0x28
    CLOSE_RANGE_ATTACK,// 0x29
    RANGED_ATTACK,// 0x2A
    MAGIC_ATTACK,// 0x2B
    ENERGY_ORB_ATTACK,// 0x2C
    TAKE_DAMAGE,// 0x2D
    GENERAL_CHAT,// 0x2E
    CLOSE_CHALKBOARD,// 0x2F
    FACE_EXPRESSION,// 0x30
    USE_ITEMEFFECT,// FACE_EXPRESSION 1
    HIRED_MERCHANT_REQUEST,// 0x3B
    NPC_TALK,// 0x36
    NPC_TALK_MORE,// 0x38
    NPC_SHOP,// 0x39
    STORAGE,// 0x3A
    DUEY_ACTION,// 0x3D
    ITEM_SORT,// 0x40
    ITEM_MOVE,// 0x42
    USE_ITEM,// 0x43
    CANCEL_ITEM_EFFECT,// 0x44
    USE_SUMMON_BAG,// 0x46
    PET_FOOD,// 0x47
    USE_MOUNT_FOOD,// 0x48
    USE_CASH_ITEM,// 0x49
    USE_CATCH_ITEM,// 0x4A
    USE_SKILL_BOOK,// 0x4B
    USE_RETURN_SCROLL,// 0x4E
    USE_UPGRADE_SCROLL,// 0x4F
    DISTRIBUTE_AP,// 0x50
    HEAL_OVER_TIME,// 0x51
    DISTRIBUTE_SP,// 0x52
    SPECIAL_MOVE,// 0x53
    CANCEL_BUFF,// 0x54
    SKILL_EFFECT,// 0x55
    MESO_DROP,// 0x56
    GIVE_FAME,// 0x57
    CHAR_INFO_REQUEST,// 0x59
    SPAWN_PET,// 0x5A
    CANCEL_DEBUFF,// 0x5B
    CHANGE_MAP_SPECIAL,// 0x5C
    USE_INNER_PORTAL,// 0x5D
    TROCK_ADD_MAP,// 0x5E
    QUEST_ACTION,// 0x62
    SKILL_MACRO,// 0x65
    REPORT,// 0x68
    USE_TREASURE_BOX,// 0x69
    PARTYCHAT,// 0x6B
    WHISPER,// 0x6C
    SPOUSECHAT,// 0x66
    MESSENGER,// 0x6E
    PLAYER_SHOP,// 0x6F
    PLAYER_INTERACTION,// 0x6F
    PARTY_OPERATION,// 0x70
    DENY_PARTY_REQUEST,// 0x71
    GUILD_OPERATION,// 0x72
    DENY_GUILD_REQUEST,// 0x73
    BUDDYLIST_MODIFY,// 0x76
    NOTE_ACTION,// 0x77
    USE_DOOR,// 0x79
    CHANGE_KEYMAP,// 0x7B
    RING_ACTION,// 0x7D
    BBS_OPERATION,// 0x86
    ENTER_MTS,// 0x87
    PET_TALK,// 0x8B
    MOVE_PET,// 0x8C
    PET_CHAT,// 0x8D
    PET_COMMAND,// 0x8E
    PET_LOOT,// 0x8F
    PET_AUTO_POT,// 0x90
    MOVE_SUMMON,// 0x94
    SUMMON_ATTACK,// 0x95
    DAMAGE_SUMMON,// 0x96
    MOVE_LIFE,// 0x9D
    AUTO_AGGRO,// 0x9E
    MONSTER_BOMB,// 0xA2
    MOB_DAMAGE_MOB,// 0xA3
    NPC_ACTION,// 0xA6
    ITEM_PICKUP,// 0xAB
    DAMAGE_REACTOR,// 0xAE
    MONSTER_CARNIVAL,// 0xB9
    PARTY_SEARCH_REGISTER,// 0xBD
    PARTY_SEARCH_START,// 0xBF
    PLAYER_UPDATE,// 0xC0
    TOUCHING_CS,// 0xC5
    BUY_CS_ITEM,// 0xC6
    COUPON_CODE,// 0xC7
    MAPLETV,// 0xD2
    MTS_TAB,// 0xD9
    MTS_OP;// 0xD9
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
        FileInputStream fis = new FileInputStream(System.getProperty("recvops"));
        props.load(fis);
        fis.close();
        return props;
    }


    static {
        try {
            ExternalCodeTableGetter.populateValues(getDefaultProperties(), values());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load recvops", e);
        }
    }
}
