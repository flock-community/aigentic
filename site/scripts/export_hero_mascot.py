"""Export the aigentic mascot from Blender into the binary the landing page hero loads.

Usage (needs Blender's Python module: `pip install bpy` on Python 3.11):
    python scripts/export_hero_mascot.py path/to/aigentic-bird.blend static/img/hero-mascot.bin
"""

import struct
import sys

import bpy
import numpy as np

ACTIONS = ["idle", "look_around", "tilt_curious", "nod_yes"]
FPS = 15
ROTATION_SCALE = 16384
TRANSLATION_SCALE = 4096
MAGIC = 0x314D4741


def srgb(linear):
    return np.where(linear <= 0.0031308, 12.92 * linear, 1.055 * np.power(np.clip(linear, 0, 1), 1 / 2.4) - 0.055)


def vertex_albedo(body, evaluated_mesh, triangles):
    uv_data = evaluated_mesh.attributes["UVMap"].data
    uvs = np.zeros(len(uv_data) * 2)
    uv_data.foreach_get("vector", uvs)
    uvs = uvs.reshape(-1, 2)
    loops = np.array([t.loops[:] for t in evaluated_mesh.loop_triangles])
    vertex_uv = np.zeros((len(body.data.vertices), 2))
    vertex_uv[triangles.reshape(-1)] = uvs[loops.reshape(-1)]
    image = next(n.image for n in body.material_slots[0].material.node_tree.nodes if n.type == "TEX_IMAGE")
    width, height = image.size
    pixels = np.array(image.pixels[:], np.float32).reshape(height, width, 4)[..., :3]
    x = np.clip((vertex_uv[:, 0] % 1.0) * (width - 1), 0, width - 1).astype(int)
    y = np.clip((vertex_uv[:, 1] % 1.0) * (height - 1), 0, height - 1).astype(int)
    color = pixels[y, x]
    if image.colorspace_settings.name != "sRGB":
        color = srgb(color)
    luminance = color @ np.array([0.3, 0.59, 0.11])
    return np.clip(luminance / 0.7, 0, 1)


def skin_weights(body, vertex_count):
    bone_index = np.zeros((vertex_count, 4), np.uint8)
    bone_weight = np.zeros((vertex_count, 4), np.int32)
    for i, vertex in enumerate(body.data.vertices):
        groups = sorted(((g.weight, g.group) for g in vertex.groups if g.weight > 0), reverse=True)[:4]
        total = sum(w for w, _ in groups)
        for j, (weight, group) in enumerate(groups):
            bone_index[i, j] = group
            bone_weight[i, j] = round(weight / total * 255)
        bone_weight[i, 0] += 255 - bone_weight[i].sum()
    return bone_index, bone_weight.clip(0, 255).astype(np.uint8)


def action_matrices(rig, bone_names, action):
    scene = bpy.context.scene
    rig.animation_data.action = action
    rig.animation_data.action_slot = action.slots[0]
    world = np.array(rig.matrix_world)
    world_inverse = np.linalg.inv(world)
    first, last = int(action.frame_range[0]), int(action.frame_range[1])
    frames = []
    frame = float(first)
    while frame <= last + 1e-6:
        scene.frame_set(int(frame), subframe=frame - int(frame))
        bones = []
        for name in bone_names:
            deform = np.array(rig.pose.bones[name].matrix @ rig.data.bones[name].matrix_local.inverted())
            bones.append((world @ deform @ world_inverse)[:3, :])
        frames.append(bones)
        frame += 30 / FPS
    matrices = np.array(frames, np.float64)
    quantized = np.empty_like(matrices)
    quantized[..., :3] = matrices[..., :3] * ROTATION_SCALE
    quantized[..., 3] = matrices[..., 3] * TRANSLATION_SCALE
    assert np.abs(quantized).max() < 32767, f"{action.name} exceeds the int16 range"
    return np.round(quantized).astype(np.int16)


def padded(data):
    raw = data.tobytes()
    return raw + b"\0" * (-len(raw) % 4)


def export(blend_path, output_path):
    bpy.ops.wm.open_mainfile(filepath=blend_path, load_ui=False, use_scripts=False)
    rig = bpy.data.objects["mascot_rig"]
    body = bpy.data.objects["mascot_body"]
    evaluated = body.evaluated_get(bpy.context.evaluated_depsgraph_get()).to_mesh()
    evaluated.calc_loop_triangles()
    triangles = np.array([t.vertices[:] for t in evaluated.loop_triangles], np.uint16)
    body_world = np.array(body.matrix_world)
    rest = np.array([v.co[:] for v in body.data.vertices])
    rest = (body_world[:3, :3] @ rest.T).T + body_world[:3, 3]
    bone_names = [g.name for g in body.vertex_groups]
    bone_index, bone_weight = skin_weights(body, len(rest))
    albedo = np.round(vertex_albedo(body, evaluated, triangles) * 255).astype(np.uint8)
    actions = [action_matrices(rig, bone_names, bpy.data.actions[name]) for name in ACTIONS]
    header = struct.pack("<6I", MAGIC, len(rest), len(triangles), len(bone_names), FPS, len(actions))
    frame_counts = struct.pack(f"<{len(actions)}I", *(len(a) for a in actions))
    sections = [rest.astype(np.float32), triangles, bone_index, bone_weight, albedo, *actions]
    with open(output_path, "wb") as out:
        out.write(header + frame_counts + b"".join(padded(s) for s in sections))
    print(f"wrote {output_path}: {len(rest)} vertices, {len(triangles)} triangles, frames {[len(a) for a in actions]}")


if __name__ == "__main__":
    export(sys.argv[1], sys.argv[2])
