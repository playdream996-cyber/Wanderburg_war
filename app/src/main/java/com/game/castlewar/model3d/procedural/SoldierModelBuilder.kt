package com.game.castlewar.model3d.procedural

import com.game.castlewar.model3d.Model

/**
 * Builds stylized Wanderburg medieval soldiers and units.
 * Supports: Swordsman, Archer, Knight, Bomber, Mage, Siege Engineer.
 */
object SoldierModelBuilder {

    /**
     * Builds a static combined Model for a given soldier class.
     * 0 = Swordsman, 1 = Archer, 2 = Knight, 3 = Bomber, 4 = Mage, 5 = Siege Engineer
     */
    fun buildSoldier(unitClass: Int = 0): Model {
        val name = when (unitClass) {
            1 -> "unit_archer"
            2 -> "unit_knight"
            3 -> "unit_bomber"
            4 -> "unit_mage"
            5 -> "unit_engineer"
            6 -> "unit_villager"
            else -> "unit_swordsman"
        }
        val builder = ProceduralModelBuilder(name)

        // 1. Chunky Stylized Legs and Boots (thickened for readability from isometric camera)
        for (sx in listOf(-1.4f, 1.4f)) {
            // Chunky Leg
            builder.addBox(1.5f, 3.6f, 1.5f, ProceduralMaterial.DARK_WOOD, tx = sx, ty = 2.4f)
            // Heavy Boot
            builder.addBox(1.8f, 1.5f, 2.4f, ProceduralMaterial.IRON, tx = sx, ty = 0.8f, tz = 0.4f)
        }

        // 2. Broad Stylized Torso / Armor
        when (unitClass) {
            2 -> {
                // Knight: Heavy Steel Plate Breastplate + Gold Pauldrons
                builder.addBox(4.8f, 5.4f, 3.2f, ProceduralMaterial.IRON, ty = 6.8f)
                builder.addBox(5.6f, 1.8f, 3.4f, ProceduralMaterial.GOLD, ty = 8.8f) // Pauldrons
                builder.addBox(4.9f, 1.2f, 3.3f, ProceduralMaterial.RED_CLOTH, ty = 4.8f) // Waist sash
            }
            4 -> {
                // Mage: Robe
                builder.addCone(3.8f, 8.0f, ProceduralMaterial.MAGIC, segments = 8, ty = 4.8f)
                builder.addBox(3.8f, 4.4f, 2.6f, ProceduralMaterial.MAGIC, ty = 7.6f)
            }
            3 -> {
                // Bomber: Padded Tunic + Rope belt
                builder.addBox(4.2f, 4.8f, 3.0f, ProceduralMaterial.DIRT, ty = 6.6f)
                builder.addBox(4.4f, 1.0f, 3.1f, ProceduralMaterial.DARK_WOOD, ty = 4.8f)
            }
            6 -> {
                // Villager: Coarse Linen Peasant Smock
                builder.addBox(3.8f, 4.6f, 2.6f, ProceduralMaterial.LIGHT_STONE, ty = 6.4f)
                builder.addBox(4.0f, 0.8f, 2.8f, ProceduralMaterial.DARK_WOOD, ty = 4.6f) // Rope belt
            }
            else -> {
                // Swordsman / Archer / Engineer: Chainmail / Leather Tunic
                val tunicMat = if (unitClass == 1) ProceduralMaterial.LEAVES_OAK else ProceduralMaterial.BLUE_CLOTH
                builder.addBox(4.2f, 4.8f, 2.8f, tunicMat, ty = 6.6f)
                builder.addBox(4.4f, 0.9f, 2.9f, ProceduralMaterial.DARK_WOOD, ty = 4.7f) // Belt
            }
        }

        // 3. Stylized Oversized Head & Expressive Helmet / Cap (Large silhouette visible at distance)
        val headRadius = 2.1f
        builder.addSphere(headRadius, ProceduralMaterial.FLESH, ty = 10.8f)

        when (unitClass) {
            0 -> {
                // Swordsman: Bold Conical Nasal Helmet with Brass Rim
                builder.addCone(2.3f, 2.6f, ProceduralMaterial.IRON, segments = 8, ty = 12.6f)
                builder.addCylinder(2.35f, 0.6f, ProceduralMaterial.GOLD, segments = 8, ty = 11.4f)
                builder.addBox(0.5f, 1.4f, 0.5f, ProceduralMaterial.IRON, ty = 10.8f, tz = 1.9f) // Nasal guard
            }
            1 -> {
                // Archer: Leather Bycocket Cap with Bold Scarlet Feather
                builder.addCone(2.2f, 2.2f, ProceduralMaterial.LEAVES_PINE, segments = 6, ty = 12.2f, rz = -15f)
                builder.addBox(0.4f, 3.2f, 0.8f, ProceduralMaterial.RED_CLOTH, tx = 1.0f, ty = 13.5f) // Feather
            }
            2 -> {
                // Knight: Imposing Great Helm with Flowing Plume
                builder.addCylinder(2.3f, 3.2f, ProceduralMaterial.IRON, segments = 8, ty = 12.0f)
                builder.addBox(2.2f, 0.5f, 2.4f, ProceduralMaterial.GOLD, ty = 11.2f) // Visor cross
                builder.addBox(0.5f, 3.2f, 2.8f, ProceduralMaterial.RED_CLOTH, ty = 14.5f) // Large red crest plume
            }
            3 -> {
                // Bomber: Ragged Peasant Hood
                builder.addSphere(2.3f, ProceduralMaterial.DARK_WOOD, ty = 11.0f)
                builder.addBox(3.0f, 1.2f, 2.8f, ProceduralMaterial.DARK_WOOD, ty = 9.8f)
            }
            4 -> {
                // Mage: Wide-brim Wizard Hat
                builder.addCylinder(3.5f, 0.5f, ProceduralMaterial.MAGIC, segments = 8, ty = 11.8f)
                builder.addCone(2.4f, 4.8f, ProceduralMaterial.MAGIC, segments = 8, ty = 14.2f, rz = -12f)
            }
            6 -> {
                // Villager: Wide straw sun hat
                builder.addCylinder(3.2f, 0.4f, ProceduralMaterial.GOLD, segments = 8, ty = 11.6f)
                builder.addCone(2.2f, 1.4f, ProceduralMaterial.GOLD, segments = 8, ty = 12.4f)
            }
            else -> {
                // Engineer: Leather Cap
                builder.addSphere(2.2f, ProceduralMaterial.DARK_WOOD, ty = 11.2f)
            }
        }

        // 4. Stylized Exaggerated Weapons & Shields (+30-35% larger for instant recognition)
        when (unitClass) {
            0 -> {
                // Swordsman: Bold Broadsword in right hand, Vibrant Heraldic Heater Shield in left
                // Right Arm
                builder.addBox(1.3f, 4.0f, 1.3f, ProceduralMaterial.FLESH, tx = 2.9f, ty = 6.6f)
                // Exaggerated Broadsword (+35% larger)
                builder.addBox(0.6f, 8.5f, 1.1f, ProceduralMaterial.IRON, tx = 3.2f, ty = 7.6f, tz = 2.5f, rx = 45f)
                builder.addBox(2.6f, 0.5f, 0.5f, ProceduralMaterial.GOLD, tx = 3.2f, ty = 5.5f, tz = 0.6f) // Crossguard
                builder.addSphere(0.6f, ProceduralMaterial.GOLD, tx = 3.2f, ty = 4.8f, tz = -0.1f) // Pommel
                // Left Arm & Exaggerated Shield
                builder.addBox(1.3f, 4.0f, 1.3f, ProceduralMaterial.FLESH, tx = -2.9f, ty = 6.6f)
                builder.addBox(0.8f, 6.8f, 4.8f, ProceduralMaterial.RED_CLOTH, tx = -3.9f, ty = 6.6f, tz = 1.0f)
                builder.addBox(0.9f, 7.0f, 0.8f, ProceduralMaterial.GOLD, tx = -3.9f, ty = 6.6f, tz = 1.0f) // Shield Cross
                builder.addBox(0.9f, 0.8f, 4.8f, ProceduralMaterial.GOLD, tx = -3.9f, ty = 6.6f, tz = 1.0f) // Shield Rim
            }
            1 -> {
                // Archer: Exaggerated Recurve Bow & Arrow Nocked
                builder.addBox(1.3f, 4.0f, 1.3f, ProceduralMaterial.FLESH, tx = -2.9f, ty = 6.6f)
                // Large Bow (+35% taller)
                builder.addCylinder(0.35f, 9.2f, ProceduralMaterial.DARK_WOOD, tx = -3.4f, ty = 7.0f, tz = 1.8f, rx = 25f)
                builder.addCylinder(0.12f, 8.8f, ProceduralMaterial.LIGHT_STONE, tx = -3.4f, ty = 7.0f, tz = 1.2f, rx = 25f) // Bowstring
                // Right Arm drawing arrow
                builder.addBox(1.3f, 4.0f, 1.3f, ProceduralMaterial.FLESH, tx = 2.9f, ty = 6.6f)
                // Quiver on back
                builder.addCylinder(1.2f, 5.8f, ProceduralMaterial.DARK_WOOD, tx = -0.7f, ty = 7.4f, tz = -1.9f, rx = -25f)
                builder.addBox(0.8f, 2.0f, 0.8f, ProceduralMaterial.RED_CLOTH, tx = -0.7f, ty = 9.8f, tz = -2.4f) // Arrow fletchings
            }
            2 -> {
                // Knight: Heavy Zweihander Greatsword and Massive Tower Greatshield
                builder.addBox(1.5f, 4.4f, 1.5f, ProceduralMaterial.IRON, tx = 3.2f, ty = 6.8f)
                // Massive Greatsword
                builder.addBox(0.7f, 10.5f, 1.4f, ProceduralMaterial.IRON, tx = 3.5f, ty = 8.0f, tz = 2.2f, rx = 35f)
                builder.addBox(3.4f, 0.7f, 0.7f, ProceduralMaterial.GOLD, tx = 3.5f, ty = 5.2f, tz = 0.5f)
                // Huge Tower Shield
                builder.addBox(1.5f, 4.4f, 1.5f, ProceduralMaterial.IRON, tx = -3.2f, ty = 6.8f)
                builder.addBox(1.0f, 9.6f, 5.4f, ProceduralMaterial.IRON, tx = -4.3f, ty = 6.4f, tz = 1.2f)
                builder.addBox(1.1f, 9.8f, 1.2f, ProceduralMaterial.GOLD, tx = -4.3f, ty = 6.4f, tz = 1.2f)
                builder.addBox(1.1f, 1.2f, 5.4f, ProceduralMaterial.GOLD, tx = -4.3f, ty = 6.4f, tz = 1.2f)
            }
            3 -> {
                // Bomber: Carrying colossal black iron bomb with burning fuse
                builder.addBox(1.3f, 3.8f, 1.3f, ProceduralMaterial.FLESH, tx = 2.2f, ty = 6.4f, tz = 1.4f, rx = 40f)
                builder.addBox(1.3f, 3.8f, 1.3f, ProceduralMaterial.FLESH, tx = -2.2f, ty = 6.4f, tz = 1.4f, rx = 40f)
                // Colossal Bomb
                builder.addSphere(3.6f, ProceduralMaterial.BOMB_BLACK, ty = 6.6f, tz = 4.2f)
                builder.addCylinder(1.0f, 1.8f, ProceduralMaterial.IRON, ty = 9.8f, tz = 4.2f)
                builder.addCylinder(0.25f, 2.0f, ProceduralMaterial.GOLD, ty = 11.4f, tz = 4.2f, rz = 25f) // Burning fuse
                builder.addSphere(0.6f, ProceduralMaterial.MAGIC, ty = 12.5f, tz = 4.2f) // Glowing spark tip
            }
            4 -> {
                // Mage: Ornate magical staff with floating crystal
                builder.addBox(1.3f, 4.0f, 1.3f, ProceduralMaterial.FLESH, tx = 2.9f, ty = 6.6f)
                builder.addCylinder(0.45f, 14.0f, ProceduralMaterial.DARK_WOOD, tx = 3.4f, ty = 8.0f, tz = 1.2f)
                builder.addSphere(1.6f, ProceduralMaterial.CRYSTAL, tx = 3.4f, ty = 15.0f, tz = 1.2f)
            }
            6 -> {
                // Villager: Carrying a market basket of grain or pitchfork
                builder.addBox(1.2f, 3.8f, 1.2f, ProceduralMaterial.FLESH, tx = 2.4f, ty = 6.2f)
                builder.addBox(1.2f, 3.8f, 1.2f, ProceduralMaterial.FLESH, tx = -2.4f, ty = 6.2f)
                // Market Basket
                builder.addBox(3.4f, 2.6f, 2.4f, ProceduralMaterial.DARK_WOOD, ty = 5.8f, tz = 2.2f)
                builder.addSphere(1.4f, ProceduralMaterial.GOLD, ty = 6.8f, tz = 2.2f) // Bread / fruit
            }
            else -> {
                // Engineer: Heavy iron sledgehammer
                builder.addBox(1.3f, 4.0f, 1.3f, ProceduralMaterial.FLESH, tx = 2.9f, ty = 6.6f)
                builder.addCylinder(0.5f, 7.5f, ProceduralMaterial.DARK_WOOD, tx = 3.4f, ty = 7.5f, tz = 1.4f, rx = 35f)
                builder.addBox(2.8f, 2.4f, 3.8f, ProceduralMaterial.IRON, tx = 3.4f, ty = 10.8f, tz = 3.0f)
            }
        }

        return builder.buildModel()
    }

    /**
     * Builds an animated ProceduralCharacterRig for real-time limb animation.
     */
    fun buildRiggedSoldier(unitClass: Int = 0): ProceduralCharacterRig {
        val root = ModelPart()

        // Torso
        val torsoMesh = PrimitiveMeshFactory.createBox(3.4f, 4.4f, 2.4f)
        val tunicMat = when (unitClass) {
            1 -> ProceduralMaterial.LEAVES_OAK
            2 -> ProceduralMaterial.IRON
            4 -> ProceduralMaterial.MAGIC
            else -> ProceduralMaterial.BLUE_CLOTH
        }
        val torsoPart = ModelPart(mesh = torsoMesh.toMesh(tunicMat), localPositionY = 4.5f)
        root.addChild(torsoPart)

        // Head
        val headMesh = PrimitiveMeshFactory.createSphere(1.4f, 4, 6)
        val headPart = ModelPart(mesh = headMesh.toMesh(ProceduralMaterial.FLESH), localPositionY = 3.6f)
        torsoPart.addChild(headPart)

        // Helmet
        val helmMesh = if (unitClass == 2) {
            PrimitiveMeshFactory.createCylinder(1.6f, 2.4f, 8)
        } else {
            PrimitiveMeshFactory.createCone(1.6f, 2.0f, 8)
        }
        val helmPart = ModelPart(mesh = helmMesh.toMesh(ProceduralMaterial.IRON), localPositionY = 1.2f)
        headPart.addChild(helmPart)

        // Left Leg
        val legMesh = PrimitiveMeshFactory.createBox(1.2f, 3.5f, 1.2f)
        val leftLegPart = ModelPart(mesh = legMesh.toMesh(ProceduralMaterial.DARK_WOOD), localPositionX = -1.1f, localPositionY = 1.8f)
        root.addChild(leftLegPart)

        // Right Leg
        val rightLegPart = ModelPart(mesh = legMesh.toMesh(ProceduralMaterial.DARK_WOOD), localPositionX = 1.1f, localPositionY = 1.8f)
        root.addChild(rightLegPart)

        // Left Arm
        val armMesh = PrimitiveMeshFactory.createBox(1.1f, 3.5f, 1.1f)
        val leftArmPart = ModelPart(mesh = armMesh.toMesh(ProceduralMaterial.FLESH), localPositionX = -2.3f, localPositionY = 1.5f)
        torsoPart.addChild(leftArmPart)

        // Right Arm
        val rightArmPart = ModelPart(mesh = armMesh.toMesh(ProceduralMaterial.FLESH), localPositionX = 2.3f, localPositionY = 1.5f)
        torsoPart.addChild(rightArmPart)

        // Weapon
        val weaponMesh = PrimitiveMeshFactory.createBox(0.5f, 6.0f, 0.8f)
        val weaponPart = ModelPart(mesh = weaponMesh.toMesh(ProceduralMaterial.IRON), localPositionY = -2.0f, localPositionZ = 1.5f, localRotationX = 45f)
        rightArmPart.addChild(weaponPart)

        // Shield
        val shieldMesh = PrimitiveMeshFactory.createBox(0.6f, 5.0f, 3.4f)
        val shieldPart = ModelPart(mesh = shieldMesh.toMesh(ProceduralMaterial.RED_CLOTH), localPositionX = -0.8f, localPositionY = -0.5f)
        leftArmPart.addChild(shieldPart)

        return ProceduralCharacterRig(
            root = root,
            headPart = headPart,
            torsoPart = torsoPart,
            leftArmPart = leftArmPart,
            rightArmPart = rightArmPart,
            leftLegPart = leftLegPart,
            rightLegPart = rightLegPart,
            weaponPart = weaponPart,
            shieldPart = shieldPart
        )
    }
}
