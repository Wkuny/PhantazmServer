package org.phantazm.core.guild.party;

import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.audience.Audience;
import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.flag.Flaggable;
import org.phantazm.core.guild.Guild;
import org.phantazm.core.guild.GuildMemberManager;
import org.phantazm.core.guild.invite.InvitationManager;
import org.phantazm.core.guild.party.notification.PartyNotification;
import org.phantazm.core.guild.permission.MultipleMemberPermission;
import org.phantazm.core.guild.permission.SingleMemberPermission;
import org.phantazm.core.player.PlayerView;

import java.util.Objects;
import java.util.function.Function;

public class Party extends Guild<PartyMember> implements Tickable {

    private final Audience audience;

    private final SpyAudience spyAudience;

    private final PartyNotification notification;

    private final InvitationManager<PartyMember> invitationManager;

    private final MultipleMemberPermission<PartyMember> kickPermission;

    private final SingleMemberPermission<PartyMember> invitePermission;

    private final SingleMemberPermission<PartyMember> allInvitePermission;

    private final SingleMemberPermission<PartyMember> joinPermission;

    private final Wrapper<PartyMember> owner;

    private final Flaggable flaggable;

    public Party(@NotNull GuildMemberManager<PartyMember> memberManager,
        @NotNull Function<? super PlayerView, ? extends PartyMember> memberCreator, @NotNull Audience audience,
        @NotNull SpyAudience spyAudience,
        @NotNull PartyNotification notification, @NotNull InvitationManager<PartyMember> invitationManager,
        @NotNull MultipleMemberPermission<PartyMember> kickPermission,
        @NotNull SingleMemberPermission<PartyMember> invitePermission,
        @NotNull SingleMemberPermission<PartyMember> allInvitePermission,
        @NotNull SingleMemberPermission<PartyMember> joinPermission, @NotNull Wrapper<PartyMember> owner, @NotNull Flaggable flaggable) {
        super(memberManager, memberCreator);
        this.audience = Objects.requireNonNull(audience);
        this.spyAudience = Objects.requireNonNull(spyAudience);
        this.notification = Objects.requireNonNull(notification);
        this.invitationManager = Objects.requireNonNull(invitationManager);
        this.kickPermission = Objects.requireNonNull(kickPermission);
        this.invitePermission = Objects.requireNonNull(invitePermission);
        this.allInvitePermission = Objects.requireNonNull(allInvitePermission);
        this.joinPermission = Objects.requireNonNull(joinPermission);
        this.owner = Objects.requireNonNull(owner);
        this.flaggable = Objects.requireNonNull(flaggable);
    }

    public @NotNull Audience getAudience() {
        return audience;
    }

    public @NotNull SpyAudience getSpyAudience() {
        return spyAudience;
    }

    public @NotNull PartyNotification getNotification() {
        return notification;
    }

    public @NotNull InvitationManager<PartyMember> getInvitationManager() {
        return invitationManager;
    }

    public @NotNull MultipleMemberPermission<PartyMember> getKickPermission() {
        return kickPermission;
    }

    public @NotNull SingleMemberPermission<PartyMember> getInvitePermission() {
        return invitePermission;
    }

    public @NotNull SingleMemberPermission<PartyMember> getAllInvitePermission() {
        return allInvitePermission;
    }

    public @NotNull SingleMemberPermission<PartyMember> getJoinPermission() {
        return joinPermission;
    }

    public @NotNull Wrapper<PartyMember> getOwner() {
        return owner;
    }

    public @NotNull Flaggable getFlaggable() {
        return flaggable;
    }

    @Override
    public void tick(long time) {
        invitationManager.tick(time);
    }
}
