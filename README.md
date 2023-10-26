___

<div align="center">

### Devastating cavalry charges, better projectiles & bones crunching against walls.

![Mod Loader](https://img.shields.io/badge/mod_loader-forge-ffe8e9?style=for-the-badge&labelColor=ffced2)
![Game Version](https://img.shields.io/badge/game_version-1.19.2-ffe8e9?style=for-the-badge&labelColor=ffced2)
![Environment](https://img.shields.io/badge/environment-server-ffe8e9?style=for-the-badge&labelColor=ffced2)

![CurseForge Downloads](https://img.shields.io/curseforge/dt/906592?style=for-the-badge&logo=curseforge&labelColor=ffceea&color=ffe8f5&link=https://www.curseforge.com/minecraft/mc-mods/velocity-based-damage-deluxe)
![GitHub issues](https://img.shields.io/github/issues/kawaiicakes/NoFortnite?style=for-the-badge&logo=github&labelColor=ffceea&color=ffe8f5&link=https://github.com/kawaiicakes/VelocityBasedDamage/issues)
![Software License](https://img.shields.io/badge/license-MIT-ffe8f5?style=for-the-badge&labelColor=ffceea&link=https://github.com/kawaiicakes/VelocityBasedDamage/blob/main/LICENSE)

![GitHub](https://img.shields.io/badge/-github-fee8ff?style=for-the-badge&logo=github&labelColor=fcceff&link=https://github.com/kawaiicakes)
![Discord](https://img.shields.io/badge/-discord-fee8ff?style=for-the-badge&logo=discord&labelColor=fcceff&link=https://www.youtube.com/watch?v=dQw4w9WgXcQ)

</div>

___

## <span style="color:orange">This mod is in beta!</span>

Highly-configurable, highly-compatible mod aiming to add more nuance and realism to damage in Minecraft. How it works is simple: the faster something is moving towards the target, the more damage it does. If the target is faster than the attacker, the less damage. That 'something' could be a sword, arrow, a horse, or a boat. Projectiles will also 'inherit' the velocity of whatever threw them, meaning throwing potions on horseback finally works properly!

Further details may be found below.

---

# 📖 Information

##### Features
- Near universal compatibility with other mods!
- Easy to configure
- Highly adaptable to your needs!
- Config with helpful descriptions!

##### Planned features
- Blacklist entities which become too overpowered!
- ALL entities take wall-collision damage!
- Entities take collision damage with other entities; not just walls!
- Vehicles take damage!
- Approach speed is added to knockback!
- Slamming into walls bounces entities back at the angle of incidence!
- MORE configurability!

##### Technical details
**Bolded** values are configurable. **Damage** is calculated with respect to the 'approach' speed of the attacker to the target. That is, the dot product of the normalized difference between attacker/target position and difference of attacker/target velocities with respect to the world. Some liberties have been taken with defining the positions of entities, however. Depending on whether the attacker is above or below the target (and vice versa), as well as the direction of velocity, position may be calculated either at the entity's "feet" or "eye" level. This was necessary to patch some unsavoury behaviours whereby a debuff is incurred in scenarios where it did not seem like there should be; despite the math being correct.

Damage calculations work based on the formula **(1/2) abs((x/a))^b,** whereas x is the approach speed, **a** is the **velocityIncrement** and **b** is the **exponentiationConstant**. The calculated value is the percent bonus of damage added on (or otherwise subtracted!). There are also the values **maxDamagePercent** and **minDamagePercent,** which let you set the max/min damage bonus allowed to be added or subtracted! You can finely tune the amounts of damage you want to let this mod dish out! Check out this [graph](https://www.desmos.com/calculator/pswnnsut10) with the default values to play around with the damage!

Damage is calculated for entities slamming into walls too! At present, this is limited to living, non-player entities. This will be fixed in a future release. Unfortunately solutions I have thus far found require this mod to be installed on clientside too for players to take damage on hitting a wall. Anyway, the **velocityThreshold** setting will allow specifying over what velocity an entity takes damage when crashing into a wall. This threshold velocity is also subtracted from the entity's velocity to determine damage done. Right now the calculation for this damage is quite lazy and will be fixed, but is directly proportional to the difference of the entity & threshold velocity.

Projectiles inheriting momentum may be configured with **projectilesHaveMomentum!** Projectiles calculate damage a little differently. As base damage to things is not affected by this mod, the crazy fast speed of projectiles often meant damage would jump up into the thousands, or even millions in the case of gun mods. This being the case, the approach speed of a projectile is by default zero unless the targeted entity has a component of velocity moving either away or towards it. This fix can be disabled by playing with the **projectileMultiplier** config option. There is also **wild mode,** which disables the nerfs necessary to not completely break some mechanics. In vanilla, arrows do damage depending on how fast they are travelling. This is disabled by default so the damage bonus is not 'double-dipped'. This again may be re-enabled by enabling wild mode.

<center><iframe src="https://www.desmos.com/calculator/xghpvey2kl?embed" width="500" height="500" style="border: 1px solid #ccc" frameborder=0></iframe></center>

___

# 💗 Credits & Thanks

This mod was inspired by [pitbox46](https://legacy.curseforge.com/members/pitbox46/projects)!
___

Also, check out my very lovely sponsor and help me take over the world!

[![Sponsor!](https://raw.githubusercontent.com/kawaiicakes/kawaiicakes.github.io/main/dedimcashley.png 'Sponsor!')](https://dedimc.promo/ashley)