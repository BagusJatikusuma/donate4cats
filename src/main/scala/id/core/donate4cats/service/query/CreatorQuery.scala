package id.core.donate4cats.service.query

import doobie.*
import doobie.implicits.*
import doobie.implicits.javatimedrivernative._
import neotype.interop.doobie.given

import id.core.donate4cats.domain.CreatorProfile
import id.core.donate4cats.domain.Member

object CreatorQuery {
  
  def getById(creatorId: String): Query0[CreatorProfile] = 
    sql"""
    SELECT id, member_id, username, display_name, bio, created_at FROM creators WHERE id = $creatorId  
    """
    .query[CreatorProfile]

  def getByUsername(username: String): Query0[CreatorProfile] =
    sql"""
    SELECT id, member_id, username, display_name, bio, created_at FROM creators WHERE username = $username
    """
    .query[CreatorProfile]

  def getByMember(member: Member): Query0[CreatorProfile] = 
    sql"""
    SELECT id, member_id, username, display_name, bio, created_at FROM creators WHERE member_id = ${member.id}
    """
    .query[CreatorProfile]

  def insert(creator: CreatorProfile): Update0 = 
    sql"""
    INSERT INTO creators(id, member_id, username, display_name, bio, created_at) 
    VALUES(${creator.id}, ${creator.memberId}, ${creator.username}, ${creator.displayName}, ${creator.bio}, ${creator.createdAt})
    """
    .update

  def save(creator: CreatorProfile): Update0 = 
    sql"""
    UPDATE creators
    SET display_name = ${creator.displayName}, bio = ${creator.bio}
    WHERE id = ${creator.id}
    """
    .update

  def delete(creator: CreatorProfile): Update0 =
    sql"""
    DELETE FROM creators WHERE id = ${creator.id}
    """
    .update

}
