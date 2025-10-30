Simple Fabric mod for dynamically reloading macros in a specific directory.
Also Includes supporting framework to structure macros.

TODO:

Expand Hooks

Expand Method API

Current Hooks:

onHUDRender(GuiGraphics graphics)

onScreenRender(Screen screen, GuiGraphics gui, int mouseX, int mouseY, float delta)

onWorldRender(WorldRenderContext context)

onScreenPreInit(Minecraft minecraft, Screen screen, int width, int height)

onScreenPostInit(Minecraft minecraft, Screen screen, int width, int height)

onKeyPress(int keyCode, int modifiers, int scanCode)

onEntityLoad(Entity enttiy, ClientLevel clientLevel)

onEntityUnload(Entity enttiy, ClientLevel clientLevel)

onChat(ChatEvent chatEvent)

onCommand(String command)

onPacketReceived(Packet<?> packet)

onPacketSent(Packet<?> packet)

onToolTipCallBack(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag, List<Component> list)

message(String text)

setMouseGrab(boolean shouldGrab)

postLevelRender(GraphicsResourceAllocator allocator, DeltaTracker tickCounter, boolean blockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, GpuBufferSlice fog, Vector4f fogColor, boolean shouldRenderSky)

preLevelRender(GraphicsResourceAllocator allocator, DeltaTracker tickCounter, boolean blockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, GpuBufferSlice fog, Vector4f fogColor, boolean shouldRenderSky)


onPlayerMove(Vec3 travelVec)
