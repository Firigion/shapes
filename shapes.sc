global_debug = false;

__dot_prod(l1, l2) -> (
	reduce(l1 * l2, _a + _, 0)
);


__cross_prod(l1, l2) -> (
	s = l(
		l1:1 * l2:2 - l2:1 * l1:2,
		l2:0 * l1:2 - l1:0 * l2:2,
		l1:0 * l2:1 - l2:0 * l1:1,
	);
);


__norm(list) -> (
 	sqrt(__dot_prod(list, list));
);


__normalize(list) -> (
	list / __norm(list);
);


__min_list(list_of_positions) -> (
	output = l();
 	for(list_of_positions:0,
		i = _i;
		list = l();
		for(list_of_positions,
			put(list, null, _:i);
		);
		put(output, null, min(list));
	);
	return(output)
);


__max_list(list_of_positions) -> (
	output = l();
 	for(list_of_positions:0,
		i = _i;
		list = l();
		for(list_of_positions,
			put(list, null, _:i);
		);
		put(output, null, max(list));
	);
	return(output)
);


__circ(p1, p2, p3) -> (
	v1 = p2-p1;
	v2 = p3-p1;

	v11 = __dot_prod(v1, v1);
	v12 = __dot_prod(v1, v2);
	v22 = __dot_prod(v2, v2);

	b = 1/ (2*(v11 * v22 - v12*v12));
	k1  = b * v22 * (v11 - v12);
	k2  = b * v11 * (v22 - v12);

	center = p1 + k1 * v1 + k2 * v2;
	l(center, __norm(center-p1));
);


sphere2(c, r, material) -> (
 	l(c1, c2, c3) = c;
 	scan(
 		c1, c2, c3,
 		r+1, r+1, r+1,
 		if( __norm(pos(_) - c) <= r, 
 			set(pos(_), material)
 		)
 	)
);


__sphere(p1, p2, p3, material, width) -> (
	l(c, r) = __circ(p1, p2, p3);
	print('c: ' + map(c, floor(_)) + ', r: ' + floor(r));
 	l(c1, c2, c3) = c;

	if( width<=1,
		(
		__tha_sphere(x, y, z, outer(r)) -> (
			x*x+y*y+z*z <= r * r
		);

		run(str('script outline %d %d %d %d %d %d %d %d %d "__tha_sphere(x, y, z)" %s', 
				c1, c2, c3, c1+r, c2+r, c3+r, c1-r, c2-r, c3-r, material
			));
		),
		(
		__tha_sphere(x, y, z, outer(r), outer(width)) -> (
			x*x+y*y+z*z <= r * r && x*x+y*y+z*z > (r-width) * (r-width)
		);

		run(str('script fill %d %d %d %d %d %d %d %d %d "__tha_sphere(x, y, z)" %s', 
				c1, c2, c3, c1+r, c2+r, c3+r, c1-r, c2-r, c3-r, material
			));
		);
	);
 	if(global_debug, (
		set(p1, 'diamond_block');
		set(p2, 'diamond_block');
		set(p3, 'diamond_block');

		set(c, 'emerald_block');
		);	
	);
);


__plane(p1, p2, p3, material, width) -> (
	v1 = p2-p1;
	v2 = p3-p1;

	l(a1, a2, a3) = __min_list(l(p1, p2, p3));
	l(b1, b2, b3) = __max_list(l(p1, p2, p3));

	n = __normalize(__cross_prod(v1, v2));
	k = __dot_prod(n, p1 - l(a1, a2, a3));

	__tha_plane(x, y, z, outer(n), outer(k), outer(width)) -> (
		__dot_prod(l(x, y, z), n) - k >= -width && __dot_prod(l(x, y, z), n) - k <= width
	);

	run(str('script fill %d %d %d %d %d %d %d %d %d "__tha_plane(x, y, z)" %s', 
			a1, a2, a3, a1, a2, a3, b1, b2, b3, material
		));

 	if(global_debug, (
		set(l(a1, a2, a3), 'gold_block');
		set(l(b1, b2, b3), 'gold_block');

		set(p1, 'diamond_block');
		set(p2, 'diamond_block');
		set(p3, 'diamond_block');
		);
 	);
);


__disc(p1, p2, p3, material, width) -> (

	l(c, r) = __circ(p1, p2, p3);
 	l(c1, c2, c3) = c;

	__tha_sphere(x, y, z, outer(r)) -> (
		x*x+y*y+z*z <= r * r
	);

	v1 = p2-p1;
	v2 = p3-p1;

	n = __normalize(__cross_prod(v1, v2));
	k = __dot_prod(n, p1 - c);

	__tha_plane(x, y, z, outer(n), outer(k), outer(width)) -> (
		__dot_prod(l(x, y, z), n) - k >= -width && __dot_prod(l(x, y, z), n) - k <= width
	);

	run(str('script fill %d %d %d %d %d %d %d %d %d "__tha_plane(x, y, z) && __tha_sphere(x, y, z)" %s', 
			c1, c2, c3, c1+r, c2+r, c3+r, c1-r, c2-r, c3-r, material
		));

 	if(global_debug, (
		set(p1, 'diamond_block');
		set(p2, 'diamond_block');
		set(p3, 'diamond_block');

		set(c, 'emerald_block');
		);
	);
);


__ring(p1, p2, p3, material, width) -> (

	l(c, r) = __circ(p1, p2, p3);
 	l(c1, c2, c3) = c;

	__tha_sphere(x, y, z, outer(r), outer(width)) -> (
		x*x+y*y+z*z <= r * r && x*x+y*y+z*z > (r-width) * (r-width)
	);

	v1 = p2-p1;
	v2 = p3-p1;

	n = __normalize(__cross_prod(v1, v2));
	k = __dot_prod(n, p1 - c);

	__tha_plane(x, y, z, outer(n), outer(k), outer(width)) -> (
		__dot_prod(l(x, y, z), n) - k >= -width && __dot_prod(l(x, y, z), n) - k <= width
	);

	run(str('script fill %d %d %d %d %d %d %d %d %d "__tha_plane(x, y, z) && __tha_sphere(x, y, z)" %s', 
			c1, c2, c3, c1+r, c2+r, c3+r, c1-r, c2-r, c3-r, material
		));

 	if(global_debug, (
		set(p1, 'diamond_block');
		set(p2, 'diamond_block');
		set(p3, 'diamond_block');

		set(c, 'emerald_block');
		);
	);
);


__line_fast(p1, p2, material, width) -> (
 	m = p2-p1;
	max_size = max(map(m, abs(_)));
	t = l(range(max_size))/max_size;
	for(t, 
 		b = m * _ + p1;
 		set(b, material);
 	);
);


__line(p1, p2, material, width) -> (
	v = p2-p1;

	if(v:2 == 0,
		n1 = l(0,0,1),
		n1 = l(1, 1, -(v:0 + v:1)/v:2);
 	);
	n1 = __normalize(n1);
	n2 = __normalize(__cross_prod(v, n1));

	__tha_plane1(x, y, z, outer(n1), outer(width)) -> (
		__dot_prod(l(x, y, z), n1) >= -width && __dot_prod(l(x, y, z), n1) <= width
	);

	__tha_plane2(x, y, z, outer(n2), outer(width)) -> (
		__dot_prod(l(x, y, z), n2) >= -width && __dot_prod(l(x, y, z), n2) <= width
	);

	run(str('script fill %d %d %d %d %d %d %d %d %d "__tha_plane1(x, y, z) && __tha_plane2(x, y, z) " %s', 
			p1:0, p1:1, p1:2, p1:0, p1:1, p1:2, p2:0, p2:1, p2:2, material
		));

);


global_positions = l(null, null, null);
global_all_set = false;
global_armor_stands = l(null, null, null);
global_show_pos = true;


__summon(i) -> (
	colours = l('red', 'lime', 'blue');
	e = spawn('armor_stand', global_positions:i+l(0.5, -1.2, 0.5), str('{ArmorItems:[{},{},{},{id:"minecraft:%s_concrete", Count:1b}], Glowing:1b, Marker:1b, Invisible:1b, Fire:32767s, CustomName:\'{"text":"pos%d"}\', CustomNameVisible:1b}',colours:i, i));
);


__mark(i, position) -> (
 	colours = l('red', 'lime', 'blue'); 
	e = create_marker('pos' + i, position + l(0.5, 0.5, 0.5), colours:(i-1) + '_concrete');
	run(str(
		'data merge entity %s {Glowing:1b, Fire:32767s, Marker:1b}', query(e, 'uuid')
		));
	put(global_armor_stands, i-1, query(e, 'id'));
);


set_pos(i) -> (
	try(
 		if( !reduce(range(1,4), _a + (_==i), 0),
			throw();
		),
		print('Input must be either 1, 2, or 3 for position to set. You input ' + i);
		return()
	);

	tha_block = query(player(), 'trace');
	if(tha_block!=null,
		tha_pos = pos(tha_block),
		tha_pos = map(pos(player()), round(_))
	);
	global_positions:(i-1) = tha_pos;
	if(all(global_positions, _!=null), global_all_set = true);

	print(str('Set your position %d to ',i) + tha_pos);

	if(global_show_pos,
		e = entity_id(global_armor_stands:(i-1));
 		if(e != null, modify(e, 'remove'));
		__mark(i, tha_pos);
	);

);


get_pos() -> (
	for(global_positions, 
 		print(str('Position %d is %s', 
				_i+1, if(_==null, 'not set', _)));
 	)
);


show_pos(b) ->(
	if(b != global_show_pos,
		if(b,
			for(global_positions, 
				if(_!=null, __mark( (_i+1) , _) ) 
			),
			for(global_armor_stands,
				e = entity_id(_);
				if(e != null, modify(e, 'remove'));
			);
		);
	);
	global_show_pos = b;
);


__drawif(shape, material, width) -> (
	if(global_all_set,
		call(shape, global_positions:0, global_positions:1, global_positions:2, material, width),
		print('Need to set all three positions first.')
	)
);


draw_disc(material, width) -> __drawif('__disc', material, width/2);
draw_ring(material, width) -> __drawif('__ring', material, width/2);
draw_sphere(material, width) -> __drawif('__sphere', material, width);
draw_plane(material, width) -> __drawif('__plane', material, width/2);
draw_line_fast(material, width) -> if(global_positions:0 != null && global_positions:1 != null, 
	__line_fast(global_positions:0, global_positions:1, material, width));
draw_line(material, width) -> if(global_positions:0 != null && global_positions:1 != null, 
	__line(global_positions:0, global_positions:1, material, width/2));