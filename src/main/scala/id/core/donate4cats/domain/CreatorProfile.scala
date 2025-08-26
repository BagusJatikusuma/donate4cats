package id.core.donate4cats.domain

final case class CreatorProfile(
  memberId: Member.Id,
  username: String,
  displayName: String,
  bio: String
)


